# THRO-SQS-MyLib
[![CI/CD](https://github.com/CP-THRO/THRO-SQS-MyLib/actions/workflows/main.yml/badge.svg)](https://github.com/CP-THRO/THRO-SQS-MyLib/actions/workflows/main.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=CP-THRO_THRO-SQS-MyLib&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=CP-THRO_THRO-SQS-MyLib)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=CP-THRO_THRO-SQS-MyLib&metric=bugs)](https://sonarcloud.io/summary/new_code?id=CP-THRO_THRO-SQS-MyLib)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=CP-THRO_THRO-SQS-MyLib&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=CP-THRO_THRO-SQS-MyLib)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=CP-THRO_THRO-SQS-MyLib&metric=coverage)](https://sonarcloud.io/summary/new_code?id=CP-THRO_THRO-SQS-MyLib)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=CP-THRO_THRO-SQS-MyLib&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=CP-THRO_THRO-SQS-MyLib)

# MyLib

**MyLib** is a personal library management system developed as part of the **Software Quality Assurance (SQS) course** at Technische Hochschule Rosenheim by **Christoph Pircher**.

[Read the Docs](https://thro-sqs-mylib.readthedocs.io/en/latest/)

## About

MyLib enables users to:

- Search for books via the [OpenLibrary API](https://openlibrary.org/developers/api)
- View book details (cover, authors, descriptions, etc.)
- Create a personal library and wishlist
- Manage personal ratings and reading status
- See an anonymized overview of books owned by all users


## Technologies Used

- **Frontend:** Vue.js, Bootstrap, TypeScript
- **Backend:** Spring Boot (Java)
- **Database:** PostgreSQL
- **Deployment:** Docker Compose
- **Authentication:** JWT

# Getting Started

## Prerequisites

- **Git** installed on your system
- **Docker** and **Docker Compose** installed

## Setup Instructions

### 1. Open a Terminal

- **Linux / macOS:** Open your preferred terminal
- **Windows:** Open PowerShell



### 2. Clone the Repository

```bash
git clone https://github.com/CP-THRO/THRO-SQS-MyLib.git
```
```bash
cd THRO-SQS-MyLib
```

### 3. Create Environment Variables
**Linux / MacOS**:
```bash
chmod +x setup.sh && ./setup.sh
```
**Windows**:
```PowerShell
./setup.ps1
```


### 4. Start the Application
**Linux / macOS**

If Docker requires sudo on your system:
```bash
sudo docker compose up -d
```
Otherwise:

```
docker compose up -d
```

**Windows (PowerShell)**

```
docker compose up -d
```

###  5. Access the Application
Once all containers are running:
Open your browser and navigate to: http://localhost:5174

### 6. Creating a User Account

To use the application:
- Click Account in the navigation bar.
- Click Sign Up.
- Register a new account with your email and password.

### 7. Shut Down the Application

To stop and remove the running containers:

```
sudo docker compose down --remove-orphans
```

Or, on Windows:

```
docker compose down --remove-orphans
```

### 8. Reset Database

Once the compose file has been started, and you want to regenerate the env files with the setup script, you have to remove the database beforehand with:
```
    sudo docker volume rm thro-sqs-mylib_db
```

#### Notes
- The backend, frontend, and PostgreSQL database are all started via Docker Compose.
- Ensure your Docker Desktop or Docker daemon is running before executing these commands.
- This guide has only been tested for Linux, since that is all I have.