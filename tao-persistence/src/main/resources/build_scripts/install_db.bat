echo off

SET mypath=%~dp0

powershell -ExecutionPolicy ByPass -File "%mypath%\install_db.ps1"

REM set /p DUMMY=Hit ENTER to continue...

