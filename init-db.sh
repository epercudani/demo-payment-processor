#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Database configuration
DB_NAME="payment_db"
DB_USER="postgres"
DB_PASSWORD="postgres"
POSTGRES_CONTAINER="payment-postgres"
REDIS_CONTAINER="payment-redis"
REDIS_PASSWORD="redis123"

echo -e "${YELLOW}Starting PostgreSQL and Redis initialization...${NC}"

# Check if Docker is installed and running
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo -e "${RED}Docker is not running. Please start Docker Desktop first.${NC}"
    exit 1
fi

# Check if PostgreSQL is already running
if docker ps | grep -q $POSTGRES_CONTAINER; then
    echo -e "${GREEN}PostgreSQL container is already running!${NC}"
    echo -e "Database: ${GREEN}$DB_NAME${NC}"
    echo -e "User: ${GREEN}$DB_USER${NC}"
    echo -e "Connection URL: ${GREEN}jdbc:postgresql://localhost:5432/$DB_NAME${NC}"
else
    # Stop and remove existing PostgreSQL container if it exists
    if docker ps -a | grep -q $POSTGRES_CONTAINER; then
        echo -e "${YELLOW}Stopping and removing existing PostgreSQL container...${NC}"
        docker stop $POSTGRES_CONTAINER
        docker rm $POSTGRES_CONTAINER
    fi

    echo -e "${YELLOW}Starting PostgreSQL container...${NC}"

    # Start new PostgreSQL container with correct configuration
    docker run --name $POSTGRES_CONTAINER \
        -e POSTGRES_DB=$DB_NAME \
        -e POSTGRES_USER=$DB_USER \
        -e POSTGRES_PASSWORD=$DB_PASSWORD \
        -p 5432:5432 \
        -d postgres:latest

    # Wait for PostgreSQL to be ready
    echo -e "${YELLOW}Waiting for PostgreSQL to be ready...${NC}"
    for i in {1..30}; do
        if docker logs $POSTGRES_CONTAINER 2>&1 | grep -q "database system is ready to accept connections"; then
            break
        fi
        if [ $i -eq 30 ]; then
            echo -e "${RED}PostgreSQL failed to start within the timeout period.${NC}"
            echo -e "${YELLOW}Container logs:${NC}"
            docker logs $POSTGRES_CONTAINER
            exit 1
        fi
        sleep 1
    done

    echo -e "${GREEN}PostgreSQL initialization completed successfully!${NC}"
    echo -e "Database: ${GREEN}$DB_NAME${NC}"
    echo -e "User: ${GREEN}$DB_USER${NC}"
    echo -e "Password: ${GREEN}$DB_PASSWORD${NC}"
    echo -e "Connection URL: ${GREEN}jdbc:postgresql://localhost:5432/$DB_NAME${NC}"
fi

# Check if Redis is already running
if docker ps | grep -q $REDIS_CONTAINER; then
    echo -e "${GREEN}Redis container is already running!${NC}"
    echo -e "Host: ${GREEN}localhost${NC}"
    echo -e "Port: ${GREEN}6379${NC}"
    echo -e "Password: ${GREEN}$REDIS_PASSWORD${NC}"
else
    # Stop and remove existing Redis container if it exists
    if docker ps -a | grep -q $REDIS_CONTAINER; then
        echo -e "${YELLOW}Stopping and removing existing Redis container...${NC}"
        docker stop $REDIS_CONTAINER
        docker rm $REDIS_CONTAINER
    fi

    echo -e "${YELLOW}Starting Redis container...${NC}"

    # Start new Redis container with password
    docker run --name $REDIS_CONTAINER \
        -e REDIS_PASSWORD=$REDIS_PASSWORD \
        -p 6379:6379 \
        -d redis:latest redis-server --requirepass $REDIS_PASSWORD

    # Wait for Redis to be ready
    echo -e "${YELLOW}Waiting for Redis to be ready...${NC}"
    for i in {1..30}; do
        if docker logs $REDIS_CONTAINER 2>&1 | grep -q "Ready to accept connections"; then
            break
        fi
        if [ $i -eq 30 ]; then
            echo -e "${RED}Redis failed to start within the timeout period.${NC}"
            echo -e "${YELLOW}Container logs:${NC}"
            docker logs $REDIS_CONTAINER
            exit 1
        fi
        sleep 1
    done

    echo -e "${GREEN}Redis initialization completed successfully!${NC}"
    echo -e "Host: ${GREEN}localhost${NC}"
    echo -e "Port: ${GREEN}6379${NC}"
    echo -e "Password: ${GREEN}$REDIS_PASSWORD${NC}"
fi 