# PowerShell script to generate .env files for backend, frontend, and database

# Generate a 32-byte random JWT secret, base64 encoded
$jwtSecretBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($jwtSecretBytes)
$JWT_SECRET = [Convert]::ToBase64String($jwtSecretBytes)

# Generate a 32-byte random password for Postgres, base64 encoded
$postgresPasswordBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($postgresPasswordBytes)
$POSTGRES_PASSWORD = [Convert]::ToBase64String($postgresPasswordBytes)

# Create .env-backend
Write-Host "Creating .env-backend"
@"
JWT_SECRET=$JWT_SECRET
POSTGRES_PASSWORD=$POSTGRES_PASSWORD
POSTGRES_HOST=postgres-db
POSTGRES_PORT=5432
BACKEND_HOST=localhost
BACKEND_PORT=8080
BACKEND_PROTO=http
FRONTEND_HOST=localhost
FRONTEND_PORT=5174
FRONTEND_PROTO=http
"@ | Set-Content -Encoding utf8NoBOM ".env-backend"

# Create .env-frontend
Write-Host "Creating .env-frontend"
@"
BACKEND_HOST=localhost
BACKEND_PORT=8080
BACKEND_PROTO=http
"@ | Set-Content -Encoding utf8NoBOM ".env-frontend"

# Create .env-db
Write-Host "Creating .env-db"
@"
POSTGRES_PASSWORD=$POSTGRES_PASSWORD
"@ | Set-Content -Encoding utf8NoBOM ".env-db"

Write-Host "Done!"
