#! /bin/sh

# Auteur : gl36
# Version initiale : 10/01/2026

CURRENT_SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
ROOT_DIR="$CURRENT_SCRIPT_DIR"
while [ "$ROOT_DIR" != "/" ] && [ ! -f "$ROOT_DIR/pom.xml" ]; do
    ROOT_DIR=$(dirname "$ROOT_DIR")
done
cd "$ROOT_DIR" || exit 1

PATH="./src/test/script/launchers:./src/main/bin:$PATH"
. "$ROOT_DIR/src/test/script/utils.sh"

section "Running CodeGen Test Suite"

# Test valid test files
VALID_SUBFOLDERS="arithmetic boolean comparison control print variables"

for sub in $VALID_SUBFOLDERS; do
    dir="$ROOT_DIR/src/test/deca/codegen/valid/$sub"
    [ -d "$dir" ] || continue; # Skip if directory doesn't exist

    step "Testing $sub valid test cases"

    for f in $dir/*.deca; do
        [ -f "$f" ] || continue # Skip if file doesn't exist
        decac "$f"
    
        ass_file=${f%.deca}.ass

        result=$(ima "$ass_file")
        expected_res=$(sed -n '/Resultats:/,/Historique:/p' "$f" | grep -vE 'Resultats:|Historique:' | sed 's/\/\/\s*//g' | sed 's/^[[:space:]]*//; s/[[:space:]]*$//' ) # extract only result from .deca file
        
        if [ "$result" = "$expected_res" ]; then
            success "Execution result is correct for $(basename "$f")"
        else
            failure "Execution result for $(basename "$f"): $result (Expected $expected_res)"
        fi
    done
done

# Test invalid files
step "Testing semantic invalid test cases (Runtime Errors)"
for f in "$ROOT_DIR/src/test/deca/codegen/invalid/arithmetic/"*.deca; do
    [ -f "$f" ] || continue # Skip if file doesn't exist
    
    decac "$f"

    ass_file=${f%.deca}.ass 

    result=$(ima "$ass_file")
    expected_res=$(sed -n '/Resultats:/,/Historique:/p' "$f" | grep -vE 'Resultats:|Historique:' | sed 's/\/\/\s*//g' | sed 's/^[[:space:]]*//; s/[[:space:]]*$//' ) # extract only result from .deca file
    
    if [ "$result" = "$expected_res" ]; then
        success "Execution correctly raised an error for $(basename "$f")"
    else
        failure "Execution result for $(basename "$f"): $result (Expected $expected_res)"
    fi
done

# Manually test stack overflow invalid cases
step "Testing stack overflow invalid test cases"

STACK_TESTS="stack-overflow-vars.deca stack-register-spill.deca"

for t in $STACK_TESTS; do 
    f="$ROOT_DIR/src/test/deca/codegen/invalid/$t"
    [ -f "$f" ] || continue
    
    info "File: $(basename "$f")"

    opt=""
    if [ "$t" = "stack-register-spill.deca" ]; then
        opt="-r 4" # use custom option to restrict register use
    fi

    decac $opt "$f"
    ass_file="${f%.deca}.ass"


    error_result=$(ima -p 4 "$ass_file")
    if [ "$error_result" = "Error: Stack overflow" ]; then
        success "Execution correctly raised error with -p 4"
    else
        failure "Execution with -p 4: $error_result (Expected Error: Stack overflow)"
    fi

    success_result=$(ima -p 5 "$ass_file")
    if [ -z "$success_result" ]; then
        success "Execution correctly did not produce output with -p 5"
    else
        failure "Execution with -p 5: $success_result (Expected no output)"
    fi
done