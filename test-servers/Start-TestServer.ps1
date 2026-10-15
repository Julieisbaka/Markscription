param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('modded', 'vanilla')]
    [string]$Server
)

Set-Variable -Name ModVersion -Value '0.3.0' -Option Constant

$ErrorActionPreference = 'Stop'
$serverDirectory = Join-Path $PSScriptRoot $Server
$java = if ($env:JAVA_HOME) {
    Join-Path $env:JAVA_HOME 'bin\java.exe'
} else {
    (Get-Command java -ErrorAction Stop).Source
}

if ($Server -eq 'modded') {
    $modFileName = "Markscription-$ModVersion.jar"
    $mod = Join-Path $PSScriptRoot "..\build\libs\$modFileName"
    if (!(Test-Path -LiteralPath $mod)) {
        throw 'Build Markscription first with .\gradlew.bat build.'
    }
    Copy-Item -LiteralPath $mod -Destination (Join-Path $serverDirectory "mods\$modFileName")
    $jar = 'fabric-server-launch.jar'
} else {
    $jar = 'server.jar'
}

if (!(Test-Path -LiteralPath (Join-Path $serverDirectory $jar))) {
    throw "Missing server JAR in $serverDirectory."
}

Write-Host "Starting $Server Minecraft 26.3 on 127.0.0.1:25565."
Write-Host 'Run only one test server at a time. Enter stop to save and shut down.'
Push-Location $serverDirectory
try {
    & $java '-Xms512M' '-Xmx2G' '-jar' $jar 'nogui'
    if ($LASTEXITCODE -ne 0) {
        throw "The $Server server exited with code $LASTEXITCODE. Check its logs\latest.log."
    }
} finally {
    Pop-Location
}
