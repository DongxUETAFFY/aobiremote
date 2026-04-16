$backendDir = Join-Path $PSScriptRoot '..\backend'
$outLogFile = Join-Path $backendDir 'backend-local-lite.out.log'
$errLogFile = Join-Path $backendDir 'backend-local-lite.err.log'

if (Test-Path $outLogFile) {
    Remove-Item $outLogFile -Force
}

if (Test-Path $errLogFile) {
    Remove-Item $errLogFile -Force
}

$process = Start-Process `
    -FilePath (Join-Path $backendDir 'mvnw.cmd') `
    -ArgumentList 'spring-boot:run', '-Dspring-boot.run.profiles=local-lite' `
    -WorkingDirectory $backendDir `
    -RedirectStandardOutput $outLogFile `
    -RedirectStandardError $errLogFile `
    -PassThru

$process.Id
