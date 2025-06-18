# Payment Processor

A payment processing system that handles various payment methods and provides transaction management capabilities.

## Features

### Core Functionality
- Payment processing and validation
- Transaction logging and monitoring
- Multiple payment gateway integrations

### Technical Stack
- Java 21
- Maven for dependency management
- PostgreSQL for transaction storage
- Redis for caching
- Spring RestTemplate for HTTP client
- Jackson for JSON processing
- SLF4J for logging

## Getting Started

### Prerequisites
- Java 21
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- Redis 6 or higher
- Docker (must be running for database scripts)

### Installation

1. Clone the repository
```bash
git clone https://github.com/your-org/payment-processor.git
```

2. Install dependencies
```bash
mvn clean install
```

3. Start the database using Docker
Run the following script to start the required PostgreSQL and Redis containers:
```bash
./init-db.sh
```
Ensure Docker is running before executing this script.

4. Configure the application
Edit `src/main/resources/application.properties` with your database and payment gateway credentials.

5. Start the application
```bash
mvn spring-boot:run
```

6. To stop the database containers, run:
```bash
./shutdown-db.sh
```

## Configuration

The system can be configured through the `application.properties` file. Key configuration options include:

- PostgreSQL database connection
- Redis cache configuration
- Payment gateway credentials
- Application settings

## Embedded Mock User Service (UserApiServer)

This application includes an embedded mock user service called `UserApiServer`, which is started automatically when the app runs. This server simulates an external user data provider and listens on port 8081.

**Purpose:**
- The `UserApiServer` is included **only for demo and development purposes**.
- It allows the application to simulate interactions with an external user service without requiring any additional setup or external dependencies.
- The server provides mock user data, introduces random delays, and occasionally simulates errors to mimic real-world service behavior.

**How it works:**
- The server starts automatically in a background thread when the main application launches.
- Your application can access user data via HTTP requests to `http://localhost:8081/users/{id}` or `http://localhost:8081/v2/users/{uuid}`.

**Note:**
- This mock service is not intended for production use. For real deployments, you should integrate with an actual user data provider.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 