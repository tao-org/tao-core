param (
    [string]$port = "5432",
    [string]$password
)
 
$PSScriptRoot = Split-Path $MyInvocation.MyCommand.Path -Parent
cd $PSScriptRoot
 
. .\common_functions.ps1

$PostgresInstallBinPath = Get-Psql-Access 
Ensure-psql-access $port $password

Execute-Sql-Command "DROP DATABASE IF EXISTS taodata;" $port ""
Execute-Sql-Command "DROP ROLE IF EXISTS tao;" $port ""

#Write-Host "Press any key to continue ..."
#$x = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")