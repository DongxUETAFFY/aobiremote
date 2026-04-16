$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path $PSScriptRoot -Parent
$backendDir = Join-Path $repoRoot 'backend'
$asciiTemp = Join-Path $repoRoot '.tmp'

New-Item -ItemType Directory -Force -Path $asciiTemp | Out-Null
$env:TEMP = $asciiTemp
$env:TMP = $asciiTemp

Set-Location $backendDir

if (Test-Path '.\mvnw.cmd') {
    & .\mvnw.cmd spring-boot:run
    if ($LASTEXITCODE -eq 0) {
        exit 0
    }

    Write-Warning "mvnw.cmd 启动失败，改用全局 mvn 重试。"
}

& mvn spring-boot:run
exit $LASTEXITCODE
