@echo off
chcp 65001 > nul
echo Dang bien dich du an...
if not exist "bin" mkdir bin
javac -encoding UTF-8 -cp "lib\mysql-connector-j-9.1.0.jar" -d bin src\main\java\com\store\model\*.java src\main\java\com\store\exception\*.java src\main\java\com\store\config\*.java src\main\java\com\store\dao\*.java src\main\java\com\store\service\*.java src\main\java\com\store\view\*.java src\main\java\com\store\Main.java
if %ERRORLEVEL% equ 0 (
    echo [OK] Bien dich thanh cong vao thu muc bin!
) else (
    echo [LOI] Bien dich that bai!
)
pause

