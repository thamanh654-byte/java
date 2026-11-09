@echo off
chcp 65001 > nul
echo ========================================================
echo        STORE MANAGEMENT - HE THONG QUAN LY CUA HANG
echo ========================================================
echo 1. Khoi dong Giao dien Do hoa Desktop (Java Swing GUI)
echo 2. Khoi dong Giao dien Dong lenh (Console Mode)
echo ========================================================
set /p choice="Nhap lua chon (1 hoac 2, mac dinh 1): "
if "%choice%"=="2" (
    java -cp "bin;lib\mysql-connector-j-9.1.0.jar" com.store.Main --console
) else (
    start javaw -cp "bin;lib\mysql-connector-j-9.1.0.jar" com.store.Main
)
