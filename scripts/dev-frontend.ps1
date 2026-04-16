$repoRoot = Split-Path $PSScriptRoot -Parent
$frontendDir = Join-Path $repoRoot 'frontend'

Set-Location $frontendDir

if (-not (Test-Path '.\node_modules')) {
    npm install
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

npm run dev -- --host 127.0.0.1 --port 5173
exit $LASTEXITCODE
