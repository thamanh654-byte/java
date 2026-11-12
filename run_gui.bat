@echo off
chcp 65001 > nul
echo Dang khoi dong Giao dien Do hoa (Swing Desktop GUI)...
set CP=bin;lib\mysql-connector-j-9.1.0.jar;lib\HikariCP-5.1.0.jar;lib\slf4j-api-2.0.13.jar;lib\slf4j-simple-2.0.13.jar
start javaw -cp "%CP%" com.store.Main

