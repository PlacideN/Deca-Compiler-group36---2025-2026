#!/bin/sh

CURRENT_SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
ROOT_DIR="$CURRENT_SCRIPT_DIR"
while [ "$ROOT_DIR" != "/" ] && [ ! -f "$ROOT_DIR/pom.xml" ]; do
    ROOT_DIR=$(dirname "$ROOT_DIR")
done
cd "$ROOT_DIR" || exit 1

PATH="./src/main/bin:$PATH"
. "$ROOT_DIR/src/test/script/utils.sh"

section "Running Contextual Analysis Test Suite"

step "Testing Valid Context Files"

VALID_FILES=$(find src/test/deca/context/valid -name "*.deca")
for f in $VALID_FILES; do
    output=$(decac -v "$f" 2>&1)
    if [ $? -eq 0 ] && [ -z "$output" ]; then
        success "$(basename "$f") didn't produce any error or warning."
    else
        failure "$(basename "$f") should be silent"
    fi
done

step "Testing Invalid Context Files"

INVALID_FILES=$(find src/test/deca/context/invalid -name "*.deca")

for f in $INVALID_FILES; do
    filename=$(basename "$f")
    header=$(grep -A 2 "Resultats:" "$f" | grep "Ligne")

    if [ -n "$header" ]; then
        # Ligne 12 : Error Message
        line=$(echo "$header" | sed -n 's/.*Ligne \([0-9]*\).*/\1/p')

        msg=$(echo "$header" | sed -n 's/.*: \(.*\)/\1/p')
    fi

    res=$(decac -v "$f" 2>&1)
    # Validate error format: filename:line:col: message
    if echo "$res" | grep -q "^$ROOT_DIR/$f:$line:"; then
        if echo "$res" | grep -qi "$msg"; then
            success "$filename produced the expected error with the correct format."
        else
            failure "Message mismatch in $filename: got $(echo "$res" | cut -d':' -f4-) (Expected: $msg)"
        fi
    else
        failure "Line mismatch in $filename: got $res (Expected line $line)"
    fi
done