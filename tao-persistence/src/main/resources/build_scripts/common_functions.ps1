param (
    [string]$port = "5432",
#    [Parameter(Mandatory=$true)][string]$username,
#    [string]$password = $( Read-Host "Input password, please" )
    [string]$password = "postgres"
)

function Invoke-Fds-WebRequest($URI) {
  	if ($PSVersionTable.PSVersion.Major -gt 2) {
        $response = Invoke-WebRequest -URI $URI
        $RecvPage = $response.AllElements
	} else {
		$WebRequest = [System.Net.WebRequest]::Create($URI)
        $WebRequest.Method = "GET"
        $WebRequest.ContentType = "application/json"
        $Response = $WebRequest.GetResponse()
        $ResponseStream = $Response.GetResponseStream()
        $ReadStream = New-Object System.IO.StreamReader $ResponseStream
        $RecvPage = $ReadStream.ReadToEnd()
	}
    return $RecvPage
}

Function ConvertTo-Json20 {
        [CmdletBinding()]
        Param(
        [Parameter(Mandatory=$True,ValueFromPipeline=$True,ValueFromPipelinebyPropertyName=$True)]
        [object]$item
        )
PROCESS {
            add-type -assembly system.web.extensions
            $ps_js=new-object system.web.script.serialization.javascriptSerializer
            return $ps_js.Serialize($item)
    }
}

function Invoke-Fds-RestMethod($Method, $body, $ContentType, $URI) {
  	if ($PSVersionTable.PSVersion.Major -gt 2) {
        $response = Invoke-RestMethod -Method $Method -Body $body -ContentType $ContentType -Uri $URI
        $RecvPage = $response
	} else {
		$WebRequest = [System.Net.WebRequest]::Create($URI)
        $WebRequest.Method = $Method
        $WebRequest.ContentType = $ContentType
        if ($Method -match "post") {
            try {
                $requestStream = $WebRequest.GetRequestStream()
                $streamWriter = New-Object System.IO.StreamWriter($requestStream)
                $streamWriter.Write($body)
            } finally {
                if ($null -ne $streamWriter) { $streamWriter.Dispose() }
                if ($null -ne $requestStream) { $requestStream.Dispose() }
            }
        }
        $Response = $WebRequest.GetResponse()
        $ResponseStream = $Response.GetResponseStream()
        $ReadStream = New-Object System.IO.StreamReader $ResponseStream
        $RecvPage = $ReadStream.ReadToEnd()
	}
    return $RecvPage
}

function Expand-ZIPFile($file, $destination)
{
    # If powershell greater than 2.0
    if ($PSVersionTable.PSVersion.Major -gt 2) {
        Add-Type -AssemblyName System.IO.Compression.FileSystem
        [System.IO.Compression.ZipFile]::ExtractToDirectory($file, $destination)
    } else {
        # If powershell 2.0
        $shell = new-object -com shell.application
        $zip = $shell.NameSpace($file)
        foreach($item in $zip.items())
        {
            $shell.Namespace($destination).copyhere($item)
        }
    }
}

# Functions
function Ensure-psql-access($port, $password) {
    # create the full path if it does not exist
    $pgPassFileFolderPath = "$env:APPDATA\postgresql\"
    if (-Not (Test-Path $pgPassFileFolderPath)) {
        New-Item -path "$env:APPDATA\postgresql\" -type directory 
        #-ErrorAction SilentlyContinue
    }
    $pgPassFile = "${pgPassFileFolderPath}\pgpass.conf"
    if (-Not (Test-Path $pgPassFile)) {
        if ($password -eq "") {
            $executeLoop = 1
            while($executeLoop -eq 1)
            {
                Write-Host "No password provided and no pgpass.conf file found. This might result in requesting password for each psql.exe call" -ForegroundColor Magenta
                $password = Read-Host -Prompt 'Please provide the password for the postgres user:'
                if ($password -ne "") {
                    $executeLoop = 0
                }
            }
        }
    }
    
    Write-Host "Using port $port and password $password" -ForegroundColor Magenta    
    if ($port -ne "" -and $password -ne "") {
        $postgresPassLine = "`nlocalhost:${port}:*:postgres:${password}"
        $postgresPassLineEscaped = "localhost:${port}:\*:postgres:*"   
        if (Test-Path $pgPassFile) {
            # the file exist, check the line
            $file = Get-Content $pgPassFile
            $containsWord = $file | %{$_ -match $postgresPassLineEscaped}
            
            If(-Not ($containsWord -contains $true))
            {
                # the line does not exist
                Write-Host "The pgpass.conf does not contains the expected line for user postgres. It will be added" -ForegroundColor Magenta
                $postgresPassLine | Out-File $pgPassFile -Append -Encoding ASCII
                #$postgresPassLine >> $pgPassFile -Encoding ASCII
                # check again the line was successfully added
                $file = Get-Content $pgPassFile
                $containsWord = $file | %{$_ -match $postgresPassLineEscaped}
                If(-Not ($containsWord -contains $true)) {
                    Write-Host "Error adding line to pgpass.conf. Try to add it manually and then reexecute script" -ForegroundColor Red
                    exit
                } else {
                    Write-Host "Postgres line successfully added to pgpass.conf." -ForegroundColor Magenta
                }
            } else {
                # nothing to do ... the line already exist
                $postgresPassLineEscaped2 = "localhost:${port}:\*:postgres:${password}"   
                $file = Get-Content $pgPassFile
                $containsWord = $file | %{$_ -match $postgresPassLineEscaped2}
                If(-Not ($containsWord -contains $true)) {
                    Write-Host "The pgpass.conf contains the expected line for user postgres (but not with the given password). The connexion to DB might fail!" -ForegroundColor Red
                } else {
                    Write-Host "The pgpass.conf contains the expected line for user postgres (expected password)." -ForegroundColor Magenta
                }
            }
        } else {
            # the file does not exist
            Write-Host "The pgpass.conf does not exist. It will be created with the expected line for user postgres" -ForegroundColor Magenta
            $postgresPassLine | Set-Content $pgPassFile
        }
    }
}

function Update-Environment3 {   
    $locations = 'HKLM:\SYSTEM\CurrentControlSet\Control\Session Manager\Environment',
                 'HKCU:\Environment'

    $locations | ForEach-Object {   
        $k = Get-Item $_
        $k.GetValueNames() | ForEach-Object {
            $name  = $_
            $value = $k.GetValue($_)
            if (-not (Test-Path env:\$name)) {
                #$env:FOO = 'bar' 
                Write-Host "Env:\$name is NOT SET!"
            } else {
                Write-Host "Env:\$name is SET with value (get-item env:$name).Value!"
            }
            Set-Item -Path Env:\$name -Value $value
            Write-Host "Set path: Env:\$name, value: $value"
        }
    }
    
    Write-Host "Set path: Env:Path is: $Env:Path"
    Write-Host "Set path: Env:PATH is: $Env:PATH"
}

function Update-Environment2 {
    $locations1 = 'HKLM:\SYSTEM\CurrentControlSet\Control\Session Manager\Environment'
    $locations2 = 'HKCU:\Environment'

    # first iterate keys from HKLM variables and save them into an hash map
    $HKLMArray = New-Object System.Collections.Specialized.OrderedDictionary
    $locations1 | ForEach-Object {   
        $k = Get-Item $_
        $k.GetValueNames() | ForEach-Object {
            $name  = $_
            $value = $k.GetValue($_)
            $HKLMArray.add($name, $value)
            Write-Host "HKLM Key: $name, value: $value"
        }
    }

    # now iterate the HKCU variables 
    $retArray = New-Object System.Collections.Specialized.OrderedDictionary
    $locations2 | ForEach-Object {   
        $k = Get-Item $_
        $k.GetValueNames() | ForEach-Object {
            $name  = $_
            $value = $k.GetValue($_)
            Write-Host "HKCU Key: $name, value: $value"
            if ($name -like "Path") {
                # if it is the Path key, then check the existing path from the HKLM
                # first set the default value to the current one (just in case in HKLM we do not have PATH set)
                $newVal = "$value"
                foreach ($HKLMelement in $HKLMArray.GetEnumerator()) {
                    $HKLMkey = $HKLMelement.Key
                    # if Path found, then concatenate paths from HKCU and HKLM
                    if ($HKLMkey -like "Path") {
                        $HKLMvalue = $HKLMelement.Value
                        $newVal = "$HKLMvalue;$value"
                        Write-Host "Setting new value for Path: $newVal"
                    }
                }
                $retArray.Set_Item($name, $newVal)                
                Write-Host "NEW_VAL: Setting name variable $name to the new value $newVal"
            } else {
                Write-Host "Just setting name variable $name to the new value $value"
                $retArray.Set_Item($name, $value)
            }
        }
    }
    # set the new variables
    foreach ($finalVar in $retArray.GetEnumerator()) {
        $varName = $finalVar.Key
        $varValue = $finalVar.Value
        Set-Item -Path Env:\$varName -Value $varValue
    }
    Write-Host "Set path: Env:Path is: $Env:Path"
}

function Update-Environment {   
    foreach($level in "Machine","User") {
       [Environment]::GetEnvironmentVariables($level).GetEnumerator() | % {
          # For Path variables, append the new values, if they're not already in there
          if($_.Name -match 'Path$') { 
             $_.Value = ($((Get-Content "Env:$($_.Name)") + ";$($_.Value)") -split ';' | Select -unique) -join ';'
          }
          $_
       } | Set-Content -Path { "Env:$($_.Name)" }
    }
}

function Get-Scripts-To-Execute($txtFile) {
    # This line is not working on Powershell 2.0
    # $retArray = [ordered]@{}
    $retArray = New-Object System.Collections.Specialized.OrderedDictionary
    
    foreach($line in Get-Content $txtFile) {
        $line = $line.Trim()
        if ($line.length -eq 0) {
            Write-Host "Ignoring empty line" -ForegroundColor Red
            continue
        }
        if ($line.StartsWith("#")) {
            Write-Host "Ignoring comment line $line" -ForegroundColor Red
            continue
        }
        Write-Host "Extracting valid line $line" -ForegroundColor Magenta
        $index = $line.IndexOf(";")
        if ($index -gt 0) {
            $sqlScript = $line.Substring(0, $index)
            $db = $line.Substring($index+1)
            $retArray.add($sqlScript, $db)
        } else {
            $sqlScript = $line.Substring(0)
            $retArray.add($sqlScript, "")
        }
#        $splitArr = $line.split(";")
#        # the first element is the sql script file name and the second (if specified) is the database
#        if ($splitArr.length -eq 1) {
#            $retArray.Add($splitArr[0], "")
#        } else {
#            if ($splitArr.length -eq 2) {
#                $retArray.Add($splitArr[0], $splitArr[1])
#            }
#        }
    }
    return $retArray
}

function Execute-Sql-Script($sqlFile, $port, $db) {
    if ($db -eq "") {
        Write-Host "Executing: $PostgresInstallBinPath -h localhost -p $port -U postgres -f $sqlFile ... " -ForegroundColor Magenta
        $execResult = & $PostgresInstallBinPath -h localhost -p $port -U postgres -f $sqlFile
    } else {
        Write-Host "Executing: $PostgresInstallBinPath -h localhost -p $port -U postgres -d $db -f $sqlFile ... " -ForegroundColor Magenta
        $execResult = & $PostgresInstallBinPath -h localhost -p $port -U postgres -d $db -f $sqlFile
    }
}

function Execute-Sql-Command($sqlCmd, $port, $db) {
    if ($db -eq "") {
        $ret = ($sqlCmd) | & $PostgresInstallBinPath -h localhost -p $port -U postgres
    } else {
        $ret = ($sqlCmd) | & $PostgresInstallBinPath -h localhost -p $port -U postgres -d $db
    }
    return $ret
}

function Get-Psql-Access {
    $PostgresInstallBinPath = "$DEFAULT_POSTGRESQL_BIN_PATH\psql.exe"
    if (Get-Command "psql.exe" -ErrorAction SilentlyContinue) 
    { 
        $PostgresInstallBinPath = Get-Command "psql.exe" | Select-Object -ExpandProperty Definition
        Write-Host "PostgreSQL found installed. Path to psql.exe is $PostgresInstallBinPath" -ForegroundColor Magenta
    } else {
        if (Test-Path "$PostgresInstallBinPath") {
            
            Write-Host "Found $PostgresInstallBinPath!!!"
        } else {
            Write-Host "Cannot find psql.exe in PATH or in the default folder!!!"
            exit
        }
    }
    return $PostgresInstallBinPath
}    

function Get-Script-Folder {
    # If powershell greater than 2.0
    if ($PSVersionTable.PSVersion.Major -gt 2) {
        return $PSScriptRoot
    } else {
        $PSScriptRoot = Split-Path $MyInvocation.MyCommand.Path -Parent
        return $PSScriptRoot
    }
}

$postgresqlPort = 5432

$DEFAULT_POSTGRESQL_PATH="C:\Program Files\PostgreSQL\9.5"
$DEFAULT_POSTGRESQL_BIN_PATH="C:\Program Files\PostgreSQL\9.5\bin"

Update-Environment

