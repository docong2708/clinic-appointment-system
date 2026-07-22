# PowerShell script to run Clinic Appointment System Microservices
# This script starts the services in their correct order and in separate windows.

# Force using local JDK 21 for compilation and runtime
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:Path = "C:\Program Files\Java\jdk-21\bin;" + $env:Path

Clear-Host
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "   CLINIC APPOINTMENT SYSTEM - STARTUP SCRIPT" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host ""

# Kill any existing Java processes to avoid port conflict
Write-Host "Stopping any lingering Java processes to free ports..." -ForegroundColor Yellow
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2

# Compilation step to ensure all classes are built (only for required modules to bypass broken services)
Write-Host "Installing shared security module... (Please wait)" -ForegroundColor Yellow
mvn clean install -pl shared/common-security -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to install common-security! Please check errors." -ForegroundColor Red
    Exit
}

Write-Host "Compiling required modules with JDK 21... (Please wait)" -ForegroundColor Yellow
mvn compile -pl shared/common-security,infra/config-server,infra/eureka-server,infra/api-gateway,services/doctor-service -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed! Please check code errors." -ForegroundColor Red
    Exit
}
Write-Host "Compilation successful!" -ForegroundColor Green
Write-Host ""

# 1. Start Config Server
Write-Host "[1/4] Starting Config Server (Port 8888)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", '$env:JAVA_HOME = ''C:\Program Files\Java\jdk-21''; $env:Path = ''C:\Program Files\Java\jdk-21\bin;'' + $env:Path; Write-Host ''CONFIG SERVER STARTING...''; mvn spring-boot:run -pl infra/config-server'
Write-Host "Waiting 12 seconds for Config Server to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 12

# 2. Start Eureka Server
Write-Host "[2/4] Starting Eureka Server (Port 8761)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", '$env:JAVA_HOME = ''C:\Program Files\Java\jdk-21''; $env:Path = ''C:\Program Files\Java\jdk-21\bin;'' + $env:Path; Write-Host ''EUREKA SERVER STARTING...''; mvn spring-boot:run -pl infra/eureka-server'
Write-Host "Waiting 10 seconds for Eureka Server to initialize..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# 3. Start API Gateway
Write-Host "[3/4] Starting API Gateway (Port 8060)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", '$env:JAVA_HOME = ''C:\Program Files\Java\jdk-21''; $env:Path = ''C:\Program Files\Java\jdk-21\bin;'' + $env:Path; Write-Host ''API GATEWAY STARTING...''; mvn spring-boot:run -pl infra/api-gateway'
Write-Host "Waiting 5 seconds..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# 4. Start Doctor Service
Write-Host "[4/4] Starting Doctor Service (Port 8082)..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", '$env:JAVA_HOME = ''C:\Program Files\Java\jdk-21''; $env:Path = ''C:\Program Files\Java\jdk-21\bin;'' + $env:Path; Write-Host ''DOCTOR SERVICE STARTING...''; mvn spring-boot:run -pl services/doctor-service'

# Additional Services (Optional)
Write-Host ""
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "Core infrastructure and Doctor Service are starting up!" -ForegroundColor Green
Write-Host "You can monitor each service in its dedicated terminal window." -ForegroundColor Gray
Write-Host "To run other business services in new windows, run:" -ForegroundColor Gray
Write-Host "  - Patient Service:     Start-Process powershell -ArgumentList '-NoExit', '-Command', 'mvn spring-boot:run -pl services/patient-service'" -ForegroundColor Gray
Write-Host "  - Appointment Service: Start-Process powershell -ArgumentList '-NoExit', '-Command', 'mvn spring-boot:run -pl services/appointment-service'" -ForegroundColor Gray
Write-Host "  - User Service:        Start-Process powershell -ArgumentList '-NoExit', '-Command', 'mvn spring-boot:run -pl services/user-service'" -ForegroundColor Gray
Write-Host "  - Notification Service:Start-Process powershell -ArgumentList '-NoExit', '-Command', 'mvn spring-boot:run -pl services/notification-service'" -ForegroundColor Gray
Write-Host "==========================================================" -ForegroundColor Green
