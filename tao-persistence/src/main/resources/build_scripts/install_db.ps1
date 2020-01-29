param (
	[string]$host = "localhost",
    [string]$port = "5432",
    [string]$password = "postgres"
)
 
$PSScriptRoot = Split-Path $MyInvocation.MyCommand.Path -Parent
cd $PSScriptRoot
 
. .\common_functions.ps1

$PostgresInstallBinPath = Get-Psql-Access 
Ensure-psql-access $host $port $password

$sqlScriptsList = Get-Scripts-To-Execute "install_db_sql_files.txt"
foreach ($element in $sqlScriptsList.GetEnumerator()) {
    $sqlScript = $element.Key
    $dbName = $element.Value
	Execute-Sql-Script $sqlScript $host $port $dbName
}

#Write-Host "Press any key to continue ..."
#$x = $host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
