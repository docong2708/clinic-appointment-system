# PowerShell script to stop Clinic Appointment System services.

param(
    [switch]$IncludeKeycloak
)

Clear-Host
Write-Host "==========================================================" -ForegroundColor Yellow
Write-Host "   CLINIC APPOINTMENT SYSTEM - STOP SCRIPT" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Yellow
Write-Host ""

$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue

if ($javaProcesses) {
    Write-Host "Stopping Java services..." -ForegroundColor Cyan
    $javaProcesses | Stop-Process -Force
    Start-Sleep -Seconds 2
    Write-Host "Java services stopped." -ForegroundColor Green
} else {
    Write-Host "No running Java services found." -ForegroundColor Gray
}

if ($IncludeKeycloak) {
    $keycloakComposePath = Join-Path $PSScriptRoot "infra\keycloak"
    if (Test-Path (Join-Path $keycloakComposePath "docker-compose.yml")) {
        Write-Host "Stopping Keycloak Docker containers..." -ForegroundColor Cyan
        Push-Location $keycloakComposePath
        try {
            docker compose down
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Keycloak Docker containers stopped." -ForegroundColor Green
            } else {
                Write-Host "Failed to stop Keycloak Docker containers." -ForegroundColor Red
            }
        } finally {
            Pop-Location
        }
    } else {
        Write-Host "Keycloak docker-compose.yml not found. Skipping Docker shutdown." -ForegroundColor Yellow
    }
}

$remainingJavaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue
Write-Host ""
if ($remainingJavaProcesses) {
    Write-Host "Some Java processes are still running:" -ForegroundColor Red
    $remainingJavaProcesses | Select-Object Id, ProcessName, MainWindowTitle | Format-Table -AutoSize
} else {
    Write-Host "All Java services are stopped." -ForegroundColor Green
}

Write-Host ""
Write-Host "Usage examples:" -ForegroundColor Gray
Write-Host "  .\stop-services.ps1" -ForegroundColor Gray
Write-Host "  .\stop-services.ps1 -IncludeKeycloak" -ForegroundColor Gray
