#!/bin/bash
# Script to install git hooks
# Run: bash .githooks/install.sh

set -e

echo "🔧 Installing git hooks..."

# Make hooks executable
chmod +x .githooks/pre-commit

# Configure git to use .githooks directory
git config core.hooksPath .githooks

echo "✅ Git hooks installed successfully!"
echo ""
echo "The following hooks are now active:"
echo "  - pre-commit: Formats code and runs Detekt before commit"
echo ""
echo "To uninstall, run: git config --unset core.hooksPath"

