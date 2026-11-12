@echo off
chcp 65001 > nul
echo Dang bien dich du an...
if not exist "bin" mkdir bin

set CP_LIBS=lib\mysql-connector-j-9.1.0.jar;lib\HikariCP-5.1.0.jar;lib\slf4j-api-2.0.13.jar;lib\slf4j-simple-2.0.13.jar

javac -encoding UTF-8 -cp "%CP_LIBS%" -d bin ^
    src\main\java\com\store\model\*.java ^
    src\main\java\com\store\exception\*.java ^
    src\main\java\com\store\util\*.java ^
    src\main\java\com\store\config\*.java ^
    src\main\java\com\store\dao\*.java ^
    src\main\java\com\store\service\*.java ^
    src\main\java\com\store\view\*.java ^
    src\main\java\com\store\Main.java

if %ERRORLEVEL% equ 0 (
    if exist "src\main\resources\db.properties" copy /y "src\main\resources\db.properties" "bin\db.properties" > nul
    echo [OK] Bien dich thanh cong vao thu muc bin!
) else (
    echo [LOI] Bien dich that bai!
)
pause

