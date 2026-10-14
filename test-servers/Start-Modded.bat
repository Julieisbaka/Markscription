@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Start-TestServer.ps1" -Server modded
pause
