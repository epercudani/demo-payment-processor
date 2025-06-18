#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

POSTGRES_CONTAINER="payment-postgres"
REDIS_CONTAINER="payment-redis"

echo -e "${YELLOW}Stopping PostgreSQL and Redis containers...${NC}"

# Check if Docker is installed and running
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker is not installed.${NC}"
    exit 1
fi

if ! docker info &> /dev/null; then
    echo -e "${RED}Docker is not running.${NC}"
    exit 1
fi

# Handle PostgreSQL container
if docker ps -a | grep -q $POSTGRES_CONTAINER; then
    if docker ps | grep -q $POSTGRES_CONTAINER; then
        echo -e "${YELLOW}Stopping PostgreSQL container...${NC}"
        docker stop $POSTGRES_CONTAINER
    fi
    echo -e "${YELLOW}Removing PostgreSQL container...${NC}"
    docker rm $POSTGRES_CONTAINER
else
    echo -e "${YELLOW}No PostgreSQL container found.${NC}"
fi

# Handle Redis container
if docker ps -a | grep -q $REDIS_CONTAINER; then
    if docker ps | grep -q $REDIS_CONTAINER; then
        echo -e "${YELLOW}Stopping Redis container...${NC}"
        docker stop $REDIS_CONTAINER
    fi
    echo -e "${YELLOW}Removing Redis container...${NC}"
    docker rm $REDIS_CONTAINER
else
    echo -e "${YELLOW}No Redis container found.${NC}"
fi

echo -e "${GREEN}All containers have been stopped and removed.${NC}" 