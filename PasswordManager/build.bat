@echo off
echo ========================================
echo  Password Manager - Build Script
echo ========================================

:: Create output directory
if not exist out mkdir out

:: Compile all Java source files
echo Compiling...
javac -d out ^
  src\interfaces\*.java ^
  src\model\*.java ^
  src\util\*.java ^
  src\core\*.java ^
  src\gui\*.java

if %errorlevel% neq 0 (
  echo.
  echo [ERROR] Compilation failed! Check the errors above.
  pause
  exit /b 1
)

echo.
echo [OK] Compilation successful!
echo.
echo Running Password Manager...
echo.
java -cp out gui.PasswordManagerGUI
pause
