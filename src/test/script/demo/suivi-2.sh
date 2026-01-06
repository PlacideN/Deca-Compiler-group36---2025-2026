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

CODEGEN="src/test/deca/codegen/valid/provided"
SYNTAX="src/test/deca/syntax/valid/provided"
CONTEXT="src/test/deca/context/valid/provided"

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
echo -e "\n${BLUE}Preview of filtered tokens:${NC}"
head -3 "$CODEGEN/moitie.lis"
echo -e "${BLUE}...${NC}"

echo -e "\nValidating token order..."
diff -b -B -u "$CODEGEN/moitie.lis" <(sed -E "s/ \[@.*='(.*)',.*\]/ \1/" "$SYNTAX/moitie-lex.ref")

if [ $? -eq 0 ]; then
    echo -e "${GREEN}${BOLD}[SUCCESS]${NC} Lexical tokens are identical to the reference."
    rm "$CODEGEN/moitie.lis"
else
    echo -e "${RED}${BOLD}[FAILURE]${NC} Tokens do not match."
    exit 1
fi

# ---------------------------------------------------------
pause

echo -e "\n${BOLD}Step 2: Syntax analysis & decompilation verification${NC}"
echo "Running test_synt on moitie.deca to generate AST..."
echo "Comparing AST structure (ignoring line/column numbers)..."
test_synt "$CODEGEN/moitie.deca" > "$CODEGEN/moitie.lis"

# Masking line/column numbers for structural comparison
diff -b -B -u <(sed -E 's/\[[0-9]+, [0-9]+\]/\[LINE, COL\]/g' "$CODEGEN/moitie.lis") \
    <(sed -E 's/\[[0-9]+, [0-9]+\]/\[LINE, COL\]/g' "$SYNTAX/moitie-synt.ref")

if [ $? -eq 0 ]; then
    echo -e "${GREEN}${BOLD}[SUCCESS]${NC} Syntax structure matches the reference."
    rm "$CODEGEN/moitie.lis"
else
    echo -e "${RED}${BOLD}[FAILURE]${NC} Syntax structure does not match the reference."
    exit 1
fi

echo -e "\nRunning 'decac -p' to reconstruct source code from AST..."
decac -p "$CODEGEN/moitie.deca" > "$CODEGEN/moitie.lis"

echo -e "\n${BLUE}Preview (Hex float & priority):${NC}"
grep "println" "$CODEGEN/moitie.lis"

echo -e "\nValidating structure (ignoring float format)..."
# Masking both hex and decimal to FLOAT for structural comparison
diff -b -B -u <(sed -e 's/0x1.0p-1/0.5/g' "$CODEGEN/moitie.lis") <(sed -e '/\/\//d' "$CODEGEN/moitie.deca")

if [ $? -eq 0 ]; then
    echo -e "${GREEN}${BOLD}[SUCCESS]${NC} Decompiled code matches the original."
    rm "$CODEGEN/moitie.lis"
else
    echo -e "${RED}${BOLD}[FAILURE]${NC} Decompiled code does not match the original."
    exit 1
fi

# ---------------------------------------------------------
pause

echo -e "\n${BOLD}Step 3: Context verification${NC}"
echo "Running test_context on moitie.deca to verify context rules..."
echo "Comparing decorated AST to reference..."

test_context "$CODEGEN/moitie.deca" > "$CODEGEN/moitie.lis"

# Masking line/column numbers for structural comparison
diff -b -B -u <(sed -E 's/\[[0-9]+, [0-9]+\]/\[LINE, COL\]/g' "$CODEGEN/moitie.lis") <(sed -E 's/\[[0-9]+, [0-9]+\]/\[LINE, COL\]/g' "$CONTEXT/moitie-context.ref")

if [ $? -eq 0 ]; then
    echo -e "${GREEN}${BOLD}[SUCCESS]${NC} Context analysis matches the reference."
    rm "$CODEGEN/moitie.lis"
else
    echo -e "${RED}${BOLD}[FAILURE]${NC} Context analysis does not match the reference."
    exit 1
fi

echo -e "\n${GREEN}${BOLD}===== All tests for moitie.deca PASSED =====${NC}"

