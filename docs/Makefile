LATEXMK = latexmk
LATEXMK_DEPS = texlive-latex-base texlive-latex-recommended texlive-latex-extra latexmk
BUILD_DIR = build
OUT_DIR = pdf

# LATEXMK_FLAGS:
# -pdf : generate pdf output file
# -interaction=nonstopmode : continue compilation without stopping for errors
# -halt-on-error : stop compilation only on fatal errors
LATEXMK_FLAGS = -pdf -interaction=nonstopmode -halt-on-error -outdir=$(BUILD_DIR)

REPORTS = user design validation

# Make all the targets PHONY to ensure they are always executed,
# even if a file with the same name already exists
.PHONY: all $(addprefix report-,$(REPORTS)) deps 

# Define custom filenames (spaces are allowed)
FILENAME_user   = manuel-utilisateur
FILENAME_design = documentation-conception
FILENAME_validation = documentation-validation

# Macro rule to build each report
define rule
report-$(1):
	@echo "Building pdf file for $(FILENAME_$(1))..."
	@mkdir -p $(1)/$(BUILD_DIR)
	@mkdir -p $(OUT_DIR)
	$(LATEXMK) $(LATEXMK_FLAGS) -cd -jobname="$(FILENAME_$(1))" $(1)/main.tex
	@mv $(1)/$(BUILD_DIR)/$(FILENAME_$(1)).pdf $(OUT_DIR)/
	@echo "PDF saved to docs/$(FILENAME_$(1)).pdf"
endef

# Generate rules for each report
$(foreach r,$(REPORTS),$(eval $(call rule,$(r))))

all: $(addprefix report-,$(REPORTS))

# Dependency installation target for Debian/Ubuntu 
deps: 
	@echo "Installing LaTeX dependencies..."
	sudo apt-get update
	sudo apt-get install -y $(LATEXMK_DEPS)

# Clean auxiliary files generated during LaTeX compilation
report-clean:
	@echo "Cleaning auxiliary LaTeX files..."
	@rm -rf $(addsuffix /$(BUILD_DIR),$(REPORTS))
	@rm -rf $(OUT_DIR)/
