Set-Location "$PSScriptRoot\..\backend"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local-lite"
