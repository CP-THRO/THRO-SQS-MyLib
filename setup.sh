#!/bin/sh

# Script for setting up the .env files for backend, frontend and database
# Generating three separate .env files, so that every service only has the information it needs


# generate a 32-byte (256-bit) JWT secret, base64 encoded
JWT_SECRET=$(head -c 32 /dev/urandom | base64)

# generate a 32-character password for the postgres database
POSTGRES_PASSWORD=$(head -c 32 /dev/urandom | base64)


# Generate .env files
echo "Creating -env-backend"
cat <<EOF > .env-backend
JWT_SECRET="$JWT_SECRET"
POSTGRES_PASSWORD="$POSTGRES_PASSWORD"
POSTGRES_HOST="postgres-db"
POSTGRES_PORT="5432"
EOF

echo "Creating -env-frontend"
cat <<EOF > .env-frontend
BACKEND_HOST="backend"
BACKEND_PORT="8080"
EOF

echo "Creating -env-db"
cat <<EOF > .env-db
POSTGRES_PASSWORD="$POSTGRES_PASSWORD"
EOF

echo "Done!"
