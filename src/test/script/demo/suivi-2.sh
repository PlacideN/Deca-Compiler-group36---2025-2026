# COLORS
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m' 

# Interaction helper
pause() {
    echo -e "\n----------------------------------------"
    read -p "Press any key to continue..."
    clear
    }

# Logging helpers
info() {
    echo -e "[${BLUE}${BOLD}INFO${NC}] ${BLUE}$1${NC}"
}

success() {
    echo -e "[${GREEN}${BOLD}SUCCESS${NC}] ${GREEN}$1${NC}"
}

failure() {
    echo -e "[${RED}${BOLD}FAILURE${NC}] ${RED}$1${NC}"
}


CODEGEN="src/test/deca/codegen/valid/provided"
SYNTAX="src/test/deca/syntax/valid/provided"
CONTEXT="src/test/deca/context/valid/provided"

# ---------------------------------------------------------
clear
echo -e "${BOLD}===== JUnit context tests =====${NC}"
mvn surefire:test

pause
echo -e "${BOLD}===== Demo for step A and B for [ExempleSansObject] =====${NC}"

# ---------------------------------------------------------
echo -e "\n${BOLD}Step 1: Lexical analysis verification${NC}"
echo "Running test_lex on moitie.deca to extract tokens..."

# Ignore token index and positions for comparison
test_lex "$CODEGEN/moitie.deca" | sed -E "s/ \[@.*='(.*)',.*\]/ \1/" > "$CODEGEN/moitie.lis"

echo "Comparing without token index and positions..."
info "Preview of filtered tokens:"
head -3 "$CODEGEN/moitie.lis"
echo -e "${BLUE}...${NC}"

echo -e "\nValidating token order..."
diff -b -B -u "$CODEGEN/moitie.lis" <(sed -E "s/ \[@.*='(.*)',.*\]/ \1/" "$SYNTAX/moitie-lex.ref")

if [ $? -eq 0 ]; then
    success "Lexical tokens are identical to the reference."
    rm "$CODEGEN/moitie.lis"
else
    failure "Tokens do not match."
    exit 1
fi

# ---------------------------------------------------------
pause

echo -e "\n${BOLD}Step 2: Syntax analysis & decompilation verification${NC}"
echo "Running test_synt on moitie.deca to generate AST..."
echo "Comparing AST structure (ignoring line/column numbers)..."
test_synt "$CODEGEN/moitie.deca" > "$CODEGEN/moitie-synt.lis"

# Masking line/column numbers for structural comparison
diff -b -B -u <(sed -E 's/\[[0-9]+, [0-9]+\]/\[LINE, COL\]/g' "$CODEGEN/moitie-synt.lis") \
    <(sed -E 's/\[[0-9]+, [0-9]+\]/\[LINE, COL\]/g' "$SYNTAX/moitie-synt.ref")

if [ $? -eq 0 ]; then
    success "Syntax structure matches the reference."
else
    failure "Syntax structure does not match the reference."
    exit 1
fi

echo -e "\nRunning 'decac -p' to reconstruct source code from AST..."
decac -p "$CODEGEN/moitie.deca" > "$CODEGEN/moitie.lis"
    
info "Preview (Hex float & priority):"
grep "println" "$CODEGEN/moitie.lis"

echo -e "\nValidating structure (ignoring float format)..."
# Masking both hex and decimal to FLOAT for structural comparison
diff -b -B -u <(sed -e 's/0x1.0p-1/0.5/g' "$CODEGEN/moitie.lis") <(sed -e '/\/\//d' "$CODEGEN/moitie.deca")

if [ $? -eq 0 ]; then
    success "Decompiled code matches the original."
    rm "$CODEGEN/moitie.lis"
else
    failure "Decompiled code does not match the original."
    exit 1
fi

# ---------------------------------------------------------
pause

echo -e "\n${BOLD}Step 3: Contextual Analysis & Decoration Verification${NC}"
echo "Running test_context on moitie.deca to verify context rules..."
echo "Comparing decorated AST to reference..."

test_context "$CODEGEN/moitie.deca" > "$CODEGEN/moitie.lis"

info "Preview of undecorated AST:"
grep -A 4 "ListDeclVar" "$CODEGEN/moitie-synt.lis"
echo -e "\t[...]"
grep -A 3 "ListExpr" "$CODEGEN/moitie-synt.lis"
echo -e "${BLUE}...${NC}"

info "Preview ConvFloat insertion:"
grep -A 8 "ListExpr" "$CODEGEN/moitie.lis"

info "Preview of definition decoration:"
grep -A 1 "Identifier (x)" "$CODEGEN/moitie.lis" | head -n 2

echo -e "\nValidating decorated AST against reference..."
# Masking line/column numbers for comparison
diff -b -B -u <(sed -E 's/\[[0-9]+, [0-9]+\]/\[LINE, COL\]/g' "$CODEGEN/moitie.lis") \
             <(sed -E 's/\[[0-9]+, [0-9]+\]/\[LINE, COL\]/g' "$CONTEXT/moitie-context.ref")

if [ $? -eq 0 ]; then
    success "Context analysis matches the reference."
    rm "$CODEGEN/moitie.lis"
    rm "$CODEGEN/moitie-synt.lis"
else
    failure "Context analysis does not match the reference."
    exit 1
fi