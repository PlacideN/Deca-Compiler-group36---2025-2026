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

section "Running Syntax Analysis Test Suite"

# Valid Syntax Files
VALID_FILES=$(find src/test/deca/syntax/valid -name "*.deca")
for f in $VALID_FILES; do

    # skip lexical tests
    case "$f" in
        *valid/lexer*) 
            continue 
            ;;
    esac
    
    decac -p "$f" > "$f.decompiled" 2> "$f.error"
    if [ $? -ne 0 ]; then
        failure "Should not fail to decompile valid file: $f"
        rm -f "$f.decompiled" "$f.error"
        continue
    fi

    test_synt "$f" | sed 's/\[[0-9]*, [0-9]*\]//g' > "$f-AST-from-original" 2>&1
    test_synt "$f.decompiled" | sed 's/\[[0-9]*, [0-9]*\]//g' > "$f-AST-from-decompiled" 2>&1
    
    if diff -b -B -Z "$f-AST-from-original" "$f-AST-from-decompiled" > /dev/null; then
        success "Decompilation preserved the AST structure for: $f"
    else
        failure "Decompilation changed the AST structure for: $f"
        diff -u "$f-AST-from-original" "$f-AST-from-decompiled"
    fi

    rm -f "$f.decompiled" "$f.error" "$f-AST-from-original" "$f-AST-from-decompiled"
done


# Invalid Syntax Files
INVALID_FILES=$(find src/test/deca/syntax/invalid -name "*.deca")
for f in $INVALID_FILES; do
    [ -e "$f" ] || continue # skip if no files found
    output=$(decac -p "$f" 2>&1)
    if [ $? -ne 0 ]; then
        success "Error correctly detected in: $f"
    else
        failure "Parser failed to detect error in: $f"
        echo "$output"
    fi
done
