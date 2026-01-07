#!/bin/sh

# Find root directory and prevent depth errors when calling other scripts
CURRENT_SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
ROOT_DIR="$CURRENT_SCRIPT_DIR"
while [ "$ROOT_DIR" != "/" ] && [ ! -f "$ROOT_DIR/pom.xml" ]; do
    ROOT_DIR=$(dirname "$ROOT_DIR")
done
cd "$ROOT_DIR" || exit 1

PATH="./src/test/script/launchers:./src/main/bin:$PATH"

# Import utils (colors, logger, etc)
. "$ROOT_DIR/src/test/script/utils.sh"

# Define paths relative to project root
CODEGEN="src/test/deca/codegen/valid/provided"
SYNTAX="src/test/deca/syntax/valid/provided"
CONTEXT="src/test/deca/context/valid/provided"

# ---------------------------------------------------------
clear
section "Syntax analysis functional tests (.deca)"
sh "$ROOT_DIR/src/test/script/test-lex.sh"

printf "\n"
section "Context analysis unit tests (JUnit)"
mvn surefire:test
pause

section "Demo for step A and B for [ExempleSansObject]"

# ---------------------------------------------------------
step "Step 1: Lexical analysis verification"
info "Running test_lex on moitie.deca to extract tokens..."

# Create temporary files for comparison to avoid relying on <() bash syntax
test_lex "$CODEGEN/moitie.deca" | sed -E "s/ \[@.*='(.*)',.*\]/ \1/" > "$CODEGEN/moitie.lis"
sed -E "s/ \[@.*='(.*)',.*\]/ \1/" "$SYNTAX/moitie-lex.ref" > "$CODEGEN/moitie-lex-ref.temp"

info "Comparing without token index and positions..."
printf "\n${BLUE}Preview of filtered tokens:${NC}\n"
head -n 3 "$CODEGEN/moitie.lis"
printf "${BLUE}...${NC}\n"

info "Validating token order..."
if diff -b -B -u "$CODEGEN/moitie.lis" "$CODEGEN/moitie-lex-ref.temp"; then
    success "Lexical tokens are identical to the reference."
    rm "$CODEGEN/moitie.lis" "$CODEGEN/moitie-lex-ref.temp"
else
    failure "Tokens do not match."
    exit 1
fi

# ---------------------------------------------------------
pause

step "Step 2: Syntax analysis & decompilation verification"
info "Running test_synt on moitie.deca to generate AST..."
test_synt "$CODEGEN/moitie.deca" > "$CODEGEN/moitie-synt.lis"

# Structural comparison with temporary files
sed -E 's/\[[0-9]+, [0-9]+\]/\[LINE, COL\]/g' "$CODEGEN/moitie-synt.lis" > "$CODEGEN/moitie-synt-lis.temp"
sed -E 's/\[[0-9]+, [0-9]+\]/\[LINE, COL\]/g' "$SYNTAX/moitie-synt.ref" > "$CODEGEN/moitie-synt-ref.temp"

if diff -b -B -u "$CODEGEN/moitie-synt-lis.temp" "$CODEGEN/moitie-synt-ref.temp"; then
    success "Syntax structure matches the reference."
    rm "$CODEGEN/moitie-synt-lis.temp" "$CODEGEN/moitie-synt-ref.temp"
else
    failure "Syntax structure does not match the reference."
    exit 1
fi

info "Running 'decac -p' to reconstruct source code..."
decac -p "$CODEGEN/moitie.deca" > "$CODEGEN/moitie.lis"
    
printf "\n${BLUE}Preview (Hex float & priority):${NC}\n"
grep "println" "$CODEGEN/moitie.lis"

info "Validating structure (ignoring float format)..."
sed -e 's/0x1.0p-1/0.5/g' "$CODEGEN/moitie.lis" > "$CODEGEN/moitie-decomp-filtered.temp"
sed -e '/\/\//d' "$CODEGEN/moitie.deca" > "$CODEGEN/moitie-src-filtered.temp"

if diff -b -B -u "$CODEGEN/moitie-decomp-filtered.temp" "$CODEGEN/moitie-src-filtered.temp"; then
    success "Decompiled code matches the original."
    rm "$CODEGEN/moitie.lis" "$CODEGEN/moitie-decomp-filtered.temp" "$CODEGEN/moitie-src-filtered.temp"
else
    failure "Decompiled code does not match the original."
    exit 1
fi

# ---------------------------------------------------------
pause

step "Step 3: Contextual Analysis & Decoration Verification"
info "Running test_context on moitie.deca..."
test_context "$CODEGEN/moitie.deca" > "$CODEGEN/moitie.lis"

# THE DEEP PREVIEWS YOU WANTED
info "Preview of undecorated AST:"
grep -A 4 "ListDeclVar" "$CODEGEN/moitie-synt.lis"
printf "\t[...]\n"
grep -A 3 "ListExpr" "$CODEGEN/moitie-synt.lis"
printf "${BLUE}...${NC}\n"

info "Preview ConvFloat insertion:"
grep -A 8 "ListExpr" "$CODEGEN/moitie.lis"

info "Preview of definition decoration:"
grep -A 1 "Identifier (x)" "$CODEGEN/moitie.lis" | head -n 2

# Comparison logic
sed -E 's/\[[0-9]+, [0-9]+\]/\[LINE, COL\]/g' "$CODEGEN/moitie.lis" > "$CODEGEN/moitie-context-lis.temp"
sed -E 's/\[[0-9]+, [0-9]+\]/\[LINE, COL\]/g' "$CONTEXT/moitie-context.ref" > "$CODEGEN/moitie-context-ref.temp"

if diff -b -B -u "$CODEGEN/moitie-context-lis.temp" "$CODEGEN/moitie-context-ref.temp"; then
    success "Context analysis matches the reference."
    rm "$CODEGEN/moitie.lis" "$CODEGEN/moitie-synt.lis" "$CODEGEN/moitie-context-lis.temp" "$CODEGEN/moitie-context-ref.temp"
    printf "\n${GREEN}${BOLD}===== All tests for moitie.deca PASSED =====${NC}\n"
else
    failure "Context analysis does not match the reference."
    rm "$CODEGEN/moitie-context-lis.temp" "$CODEGEN/moitie-context-ref.temp"
    exit 1
fi