# ============================================================
#  run-backend.ps1 — Sobe a API Spring Boot (Xadrez Online)
#  Uso: .\run-backend.ps1
# ============================================================

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$envFile   = Join-Path $scriptDir ".env"

# 1. Carregar variaveis do .env
if (Test-Path $envFile) {
    Write-Host "Carregando variaveis de $envFile..." -ForegroundColor Cyan
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith('#')) {
            $parts = $line -split '=', 2
            if ($parts.Count -eq 2) {
                $key   = $parts[0].Trim()
                $value = $parts[1].Trim()
                [System.Environment]::SetEnvironmentVariable($key, $value, 'Process')
                Write-Host "  OK: $key" -ForegroundColor DarkGray
            }
        }
    }
} else {
    Write-Warning "Arquivo .env nao encontrado em $envFile. Usando valores padrao do application.yml."
}

# 2. Verificar se Java esta disponivel
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Error "Java nao encontrado. Instale o JDK 17+ e adicione ao PATH."
    exit 1
}
$javaVersion = (java -version 2>&1 | Select-String 'version "(.+)"').Matches[0].Groups[1].Value
Write-Host "Java detectado: $javaVersion" -ForegroundColor Green

# 3. Determinar a porta
$porta = $env:PORT
if (-not $porta) {
    $porta = '8080'
}

# 4. Rodar o Spring Boot via Gradle
Write-Host ""
Write-Host "Iniciando backend na porta $porta..." -ForegroundColor Yellow
Write-Host "   API disponivel em: http://localhost:$porta" -ForegroundColor Cyan
Write-Host "   Swagger UI:        http://localhost:$porta/swagger-ui.html" -ForegroundColor Cyan
Write-Host ""

Set-Location $scriptDir
.\gradlew.bat bootRun
