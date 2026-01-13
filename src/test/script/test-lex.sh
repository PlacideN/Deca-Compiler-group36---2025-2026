#! /bin/sh

# Auteur : gl36
# Version initiale : 07/01/2026

CURRENT_SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
ROOT_DIR="$CURRENT_SCRIPT_DIR"
while [ "$ROOT_DIR" != "/" ] && [ ! -f "$ROOT_DIR/pom.xml" ]; do
    ROOT_DIR=$(dirname "$ROOT_DIR")
done
cd "$ROOT_DIR" || exit 1

PATH="./src/test/script/launchers:./src/main/bin:$PATH"

# Import utils (colors, logger, etc)
. "$ROOT_DIR/src/test/script/utils.sh"

section "Running Lexical Analysis Test Suite"

# Valid Lexical Files
VALID_FILES=$(find src/test/deca/syntax/valid -name "*.deca")
for f in $VALID_FILES; do
    [ -e "$f" ] || continue # skip if no files found
    output=$(test_lex "$f" 2>&1)
    if [ $? -ne 0 ]; then
        failure "Lexical error in valid file: $f"
        printf "%s\n" "$output"
    else
        success "No lexical error in: $(basename "$f")"
    fi
done


# Invalid Lexical Files
INVALID_FILES=$(find src/test/deca/syntax/invalid \( -path "*/lexer/*" -o -name "*-lex.deca" \) -name "*.deca");
for f in $INVALID_FILES; do
    [ -e "$f" ] || continue # skip if no files found
    output=$(test_lex "$f" 2>&1)
    if [ $? -ne 0 ]; then
        success "Error correctly detected in: $f"
    else
        failure "Lexer failed to detect error in: $f"
        echo "$output"
    fi
done
