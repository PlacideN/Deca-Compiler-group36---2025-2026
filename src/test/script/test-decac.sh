#! /bin/sh

# Auteur : gl36
# Version initiale : 10/01/2026

PATH="./src/main/bin:$PATH"
. "$PWD/src/test/script/utils.sh"

section "Testing decac CLI Options"

# use a temporary directory to store .deca, .lis and .res files
TMP_DIR="/tmp/decac-cli-$$"
mkdir -p "$TMP_DIR"

step "Testing -b (Banner)"
output=$(decac -b)
if [ $? -eq 0 ] && [ -n "$output" ]; then
    success "Banner displayed correctly"
else
    failure "Banner failed or empty"
fi

step "Testing decac without arguments (Help)"
output=$(decac 2>&1) # capture stderr output to show it in stdout
if echo "$output" | grep -q "Usage: decac"; then # don't let grep print anything with -q
    success "Command usage displayed when called without args"
else
    failure "No usage info identified. Displayed output: $output"
fi

step "Testing -p (Decompilation circularity)"
VALID_FILE="src/test/deca/codegen/valid/arithmetic/arith-priority.deca"
[ -f "$VALID_FILE" ] || { failure "Test file $VALID_FILE doesn't exist" ; exit 1 ; }

decac -p "$VALID_FILE" > "$TMP_DIR/decompiled.deca"
if [ $? -eq 0 ]; then
    if decac -v "$TMP_DIR/decompiled.deca" 2>&1; then
        success "-p produced valid Deca code for $(basename "$VALID_FILE")"
    else
        failure "-p output has generated incorrect syntax for $(basename "$VALID_FILE")"
    fi
else
    failure "decac -p failed to run on $VALID_FILE"
fi


step "Testing -v (Contextual Verification)"

VALID_FILES=$(find src/test/deca/context/valid -name "*.deca" | head -n 5) # limit to 5 files
for f in $VALID_FILES; do
    if decac -v "$f" 2>&1 | grep -q "."; then
        failure "-v should be silent on valid file: $f"
    else
        count=$((count + 1))
    fi
done

if [ $count -eq 5 ]; then
    success "All valid files passed verification with -v"
else
    failure "Some valid files failed verification with -v, see above."
fi

count=0 # reset counter

INVALID_FILES=$(find src/test/deca/context/invalid -name "*.deca" | head -n 5)
for f in $INVALID_FILES; do
    if decac -v "$f" 2>&1 | grep -qiE ":[0-9]+:[0-9]+:"; then
        count=$((count + 1))
    else
        failure "-v failed to report error in $f or error format is incorrect."
    fi
done

if [ $count -eq 5 ]; then
    success "All invalid files reported errors correctly with -v"
else
    failure "Some invalid files did not report errors correctly with -v, see above."
fi

count=0 # reset counter

step "Testing -r (Register count limit)"
for num in $(seq 4 16); do
    if decac -r "$num" "$VALID_FILE" > /dev/null 2>&1; then
        count=$((count + 1))
    else
        failure "Failed to run decac -r $num in $VALID_FILE"
    fi
    if [ $num -eq 16 ]; then
        success "decac -r works in the accepted range (4-16)"
    fi
done

# test out of range values
for num in 0 3 17; do
    if decac -r "$num" "$VALID_FILE" > /dev/null 2>&1; then
        failure "Accepted invalid -r $num"
    else
        success "Rejected invalid -r $num"
    fi
done

step "Testing .ass file placement"
cp "$VALID_FILE" "$TMP_DIR/valid.deca"
decac "$TMP_DIR/valid.deca"
if [ -f "$TMP_DIR/valid.ass" ]; then
    success ".ass file created in source directory"
else
    failure ".ass file was not created next to .deca file"
fi

step "Testing -n (Skip 11.1 and 11.3 runtime checks)"
NO_CHECK_TESTS="arithmetic/arith-div-zero-int.deca stack-overflow-vars.deca"

for t in $NO_CHECK_TESTS; do
    f="src/test/deca/codegen/invalid/$t"
    [ -f "$f" ] || continue

    info "File: $(basename "$f")"

    decac "$f"
    mv "${f%.deca}.ass" "$TMP_DIR/with-check.ass"
    decac -n "$f"
    mv "${f%.deca}.ass" "$TMP_DIR/no-check.ass"

    ima_options=""
    expected_err="Error: Division by zero"
    if [ "$(basename "$f")" = "stack-overflow-vars.deca" ]; then
        ima_options="-p 4"
        expected_err="Error: Stack overflow"
    fi

    # assembly instructions check
    if grep -q "ERROR" "$TMP_DIR/with-check.ass"; then
        if ! grep -q "ERROR" "$TMP_DIR/no-check.ass"; then
            success "-n correctly removed ERROR instructions in .ass file"
        else
            failure "-n failed to remove ERROR instructions in .ass file"
        fi
    else
        failure "ERROR instruction not found in standard compilation"
    fi

    # runtime check
    out_no_n=$(ima $ima_options "$TMP_DIR/with-check.ass")
    out_n=$(ima $ima_options "$TMP_DIR/no-check.ass")

    if [ "$out_no_n" = "$expected_err" ]; then
        if [ -z "$out_n" ]; then # got no output with -n
            success "Error suppressed during runtime with -n"
        else
            failure "Error NOT suppressed (got: $out_n)"
        fi
    else
        failure "Standard compilation didn't print expected error (got: $out_no_n)"
    fi
done

rm -rf "$TMP_DIR"