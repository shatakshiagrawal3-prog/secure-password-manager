#!/bin/bash
echo "========================================"
echo " Password Manager - Build Script"
echo "========================================"

# Create output directory
mkdir -p out

# Compile all Java source files
echo "Compiling..."
javac -d out \
  src/interfaces/*.java \
  src/model/*.java \
  src/util/*.java \
  src/core/*.java \
  src/gui/*.java

if [ $? -ne 0 ]; then
  echo ""
  echo "[ERROR] Compilation failed! Check the errors above."
  exit 1
fi

echo ""
echo "[OK] Compilation successful!"
echo ""
echo "Running Password Manager..."
echo ""
java -cp out gui.PasswordManagerGUI
