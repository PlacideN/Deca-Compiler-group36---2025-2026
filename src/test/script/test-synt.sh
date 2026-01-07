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

step "Running Syntax Analysis Test Suite"

# Valid Syntax Files
for f in src/test/deca/syntax/valid/*.deca; do
    [ -e "$f" ] || continue 

    # Since all keywords are not yet implented (for the no-object sublanguage) 
    # skip files using them
    case "$f" in
        *_lex*) continue ;;
    esac

    test_synt "$f" > "$f-synt.temp" 2>&1
    if [ $? -ne 0 ]; then
        failure "Real error in valid syntax file: $f"
        printf "%s\n" "$output"
        continue
    fi

    ref=$(printf "%s" "$f" | sed 's/\.deca/.ref/') # check for a corresponding .ref file
    if [ -f "$ref" ]; then
        info "Comparing $f against reference..."

        if diff -b -B -Z "$f-synt.temp" "$ref"; then
            success "$f The AST is identical to the reference."
        else
            failure "$f The AST differs from the reference."
        fi
    else
        success "No reference found, skipping comparison for $f"
    fi
    rm "$f-synt.temp"
done


# Run also the provided tests
for f in src/test/deca/syntax/valid/provided/*.deca; do
    [ -e "$f" ] || continue 
    test_synt "$f" > /dev/null 2>&1 # /dev/null runs silently
    if [ $? -eq 0 ]; then
        success "$f"
    else
        failure "Real error in valid syntax file: $f"
    fi
done

# Invalid Syntax Files
for f in src/test/deca/syntax/invalid/*_parse.deca; do
    [ -e "$f" ] || continue # skip if no files found
    output=$(test_synt "$f" 2>&1)
    if [ $? -ne 0 ]; then
        success "Error correctly detected in: $f"
    else
        failure "Parser failed to detect error in: $f"
        echo "$output"
    fi
done

# Run also the provided tests
for f in src/test/deca/syntax/invalid/provided/*_parse.deca; do
    [ -e "$f" ] || continue
    test_synt "$f" > /dev/null 2>&1;
    if [ $? -ne 0 ]; then
        success "Error correctly detected in: $f"
    else
        failure "Parser failed to detect error in: $f"
    fi
done