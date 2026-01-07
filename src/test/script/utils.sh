#!/bin/sh

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m' 

# Interaction helper
pause() {
printf "\n----------------------------------------\n"
    printf "Press enter to continue..."
    read dummy 
    clear
}

# Logging helpers
info() { printf "[${BLUE}${BOLD}INFO${NC}] ${BLUE}$1${NC}\n"; }
success() { printf "[${GREEN}${BOLD}SUCCESS${NC}] ${GREEN}$1${NC}\n"; }
failure() { printf "[${RED}${BOLD}FAILURE${NC}] ${RED}$1${NC}\n"; }
section() { printf "${BOLD}===== $1 =====${NC}\n"; }
step() { printf "\n${BOLD}$1${NC}\n"; }