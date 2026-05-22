# ============================================================
#  run-backend.ps1 — Sobe a API Spring Boot (Xadrez Online)
#  Uso: .\run-backend.ps1
# ============================================================

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$envFile   = Join-Path $scriptDir ".env"

# 1. Carregar variáveis do .env ─────────────────────────────
if (Test-Path $envFile) {
    Write-Host "📄 Carregando variáveis de $envFile..." -ForegroundColor Cyan
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith('#')) {
            $parts = $line -split '=', 2
            if ($parts.Count -eq 2) {
                $key   = $parts[0].Trim()
                $value = $parts[1].Trim()
                [System.Environment]::SetEnvironmentVariable($key, $value, 'Process')
                Write-Host "  ✔ $key" -ForegroundColor DarkGray
            }
        }
    }
} else {
    Write-Warning "Arquivo .env não encontrado em $envFile. Usando valores padrão do application.yml."
}

# 2. Verificar se Java está disponível ─────────────────────
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Error "❌ Java não encontrado. Instale o JDK 17+ e adicione ao PATH."
    exit 1
}
$javaVersion = (java -version 2>&1 | Select-String 'version "(.+)"').Matches[0].Groups[1].Value
Write-Host "☕ Java detectado: $javaVersion" -ForegroundColor Green

# 3. Rodar o Spring Boot via Gradle ────────────────────────
Write-Host ""
Write-Host "🚀 Iniciando backend na porta $($env:PORT ?? '8080')..." -ForegroundColor Yellow
Write-Host "   API disponível em: http://localhost:$($env:PORT ?? '8080')" -ForegroundColor Cyan
Write-Host "   Swagger UI:        http://localhost:$($env:PORT ?? '8080')/swagger-ui.html" -ForegroundColor Cyan
Write-Host ""

Set-Location $scriptDir
.\gradlew.bat bootRun
