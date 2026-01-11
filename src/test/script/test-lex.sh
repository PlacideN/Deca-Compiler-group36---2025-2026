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
for f in src/test/deca/syntax/valid/*.deca; do
    [ -e "$f" ] || continue # skip if no files found
    output=$(test_lex "$f" 2>&1)
    if [ $? -ne 0 ]; then
        failure "Real error in valid lex file: $f"
        printf "%s\n" "$output"
    fi
done

# Run also the provided tests
for f in src/test/deca/syntax/valid/provided/*.deca; do
    [ -e "$f" ] || continue 
    test_lex "$f" > /dev/null 2>&1 # /dev/null runs silently
    if [ $? -eq 0 ]; then
        success "$f"
    else
        failure "Real error in valid lex file: $f"
    fi
done

# Invalid Lexical Files
for f in src/test/deca/syntax/invalid/*-lex.deca; do
    [ -e "$f" ] || continue # skip if no files found
    output=$(test_lex "$f" 2>&1)
    if [ $? -ne 0 ]; then
        success "Error correctly detected in: $f"
    else
        failure "Lexer failed to detect error in: $f"
        echo "$output"
    fi
done

# Run also the provided tests
for f in src/test/deca/syntax/invalid/provided/*-lex.deca; do
    [ -e "$f" ] || continue
    test_lex "$f" > /dev/null 2>&1;
    if [ $? -ne 0 ]; then
        success "Error correctly detected in: $f"
    else
        failure "Lexer failed to detect error in: $f"
    fi
done