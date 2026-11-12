@echo off
chcp 65001 > nul
echo ========================================================
echo        STORE MANAGEMENT - HE THONG QUAN LY CUA HANG
echo ========================================================
echo 1. Khoi dong Giao dien Do hoa Desktop (Java Swing GUI)
echo 2. Khoi dong Giao dien Dong lenh (Console Mode)
echo ========================================================
set /p choice="Nhap lua chon (1 hoac 2, mac dinh 1): "
set CP=bin;lib\mysql-connector-j-9.1.0.jar;lib\HikariCP-5.1.0.jar;lib\slf4j-api-2.0.13.jar;lib\slf4j-simple-2.0.13.jar
if "%choice%"=="2" (
    java -cp "%CP%" com.store.Main --console
) else (
    start javaw -cp "%CP%" com.store.Main
)
