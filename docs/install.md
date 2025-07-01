# Getting Started

This page describes how to set up and run MyLib locally.


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