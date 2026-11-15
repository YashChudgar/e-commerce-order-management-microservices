# E-Commerce Order Management System Using Spring Boot Microservices

A backend-focused e-commerce order management system developed using Spring Boot Microservices. The application separates customer management, product management, order processing and authentication into independent services, each with its own PostgreSQL database.

An API Gateway provides a single entry point to the application and handles JWT-based authentication for protected endpoints.

## Project Overview

The system allows users to:

- Register and authenticate using JWT.
- Create, retrieve, update and delete customer information.
- Manage products and product stock.
- Place orders containing multiple products.
- Check product availability before placing orders.
- Reserve and release product stock.
- Cancel pending orders and restore reserved stock.
- Access services through a central API Gateway.

## Architecture

The application consists of five independent services:

| Service | Port | Database |
|---|---:|---|
| API Gateway | 8080 | — |
| Customer Service | 8081 | customer_db |
| Product Service | 8082 | product_db |
| Order Service | 8083 | order_db |
| Authentication Service | 8084 | auth_db |

### Architecture Diagram

```text
                      Client / Postman
                             |
                             v
                     API Gateway :8080
                     JWT Authentication
                             |
          +------------------+------------------+
          |                  |                  |
          v                  v                  v
   Customer Service   Product Service    Order Service
       :8081              :8082               :8083
          |                  |                  |
          v                  v                  v
     customer_db         product_db          order_db
                             ^
                             |
                     Stock Reservation
                       and Stock Release

                    Authentication Service
                            :8084
                             |
                             v
                          auth_db
```

The Order Service communicates with the Customer and Product Services using REST APIs through Spring's `RestClient`.

Each business service maintains its own database to support independent data ownership.

## Technologies Used

- Java 21
- Spring Boot 4.x
- Spring Data JPA
- Spring Web MVC
- Spring Validation
- Spring Cloud Gateway
- Spring Security
- JWT Authentication
- PostgreSQL
- Maven
- Swagger / OpenAPI
- Postman
- Visual Studio Code
- Git and GitHub

## Microservices

### 1. Customer Service

**Port:** 8081  
**Database:** customer_db

Responsible for customer management.

Main operations:

- Create a customer.
- Retrieve all customers.
- Retrieve a customer by ID.
- Update customer details.
- Delete a customer.
- Search customers by name or email.

Base endpoint: `/api/customers`

### 2. Product Service

**Port:** 8082  
**Database:** product_db

Responsible for product management and stock operations.

Main operations:

- Create and retrieve products.
- Update and delete products.
- Search and filter products.
- Reserve product stock.
- Release previously reserved stock.
- Prevent reservations when sufficient stock is unavailable.

Base endpoint: `/api/products`

### 3. Order Service

**Port:** 8083  
**Database:** order_db

Responsible for order processing.

Main operations:

- Create orders.
- Retrieve individual orders.
- Retrieve all orders.
- Cancel pending orders.
- Restore reserved product stock when an order is cancelled.
- Communicate with Customer and Product Services to validate order information.

Base endpoint: `/api/orders`

### 4. Authentication Service

**Port:** 8084  
**Database:** auth_db

Responsible for user registration and authentication.

Main operations:

- Register users.
- Authenticate users.
- Generate JWT access tokens.
- Securely store passwords using BCrypt hashing.

Base endpoint: `/api/auth`

### 5. API Gateway

**Port:** 8080

Provides a single entry point for accessing the microservices.

Responsibilities:

- Route requests to the appropriate microservice.
- Validate JWT access tokens for protected endpoints.
- Provide centralised access to the application's APIs.
- Provide centralised Swagger documentation for all four services.

Gateway routes:

| Route | Destination |
|---|---|
| `/api/customers/**` | Customer Service |
| `/api/products/**` | Product Service |
| `/api/orders/**` | Order Service |
| `/api/auth/**` | Authentication Service |

## Important API Endpoints

All requests can be sent through the API Gateway:

`http://localhost:8080`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Authenticate a user |
| GET | `/api/customers` | Retrieve customers |
| POST | `/api/customers` | Create a customer |
| GET | `/api/customers/{id}` | Retrieve a customer |
| PUT | `/api/customers/{id}` | Update a customer |
| DELETE | `/api/customers/{id}` | Delete a customer |
| GET | `/api/products` | Retrieve products |
| POST | `/api/products` | Create a product |
| GET | `/api/products/{id}` | Retrieve a product |
| PUT | `/api/products/{id}` | Update a product |
| DELETE | `/api/products/{id}` | Delete a product |
| POST | `/api/products/{productId}/reservations` | Reserve product stock |
| DELETE | `/api/products/{productId}/reservations/{reservationId}` | Release reserved stock |
| POST | `/api/orders` | Create an order |
| GET | `/api/orders` | Retrieve all orders |
| GET | `/api/orders/{id}` | Retrieve an order |
| PATCH | `/api/orders/{id}/cancel` | Cancel a pending order |

Protected endpoints require a valid JWT access token in the Authorization header:

```text
Authorization: Bearer <your-access-token>
```

Registration and login endpoints are publicly accessible.

## Order Processing and Stock Management

The Order Service coordinates the order creation process:

1. Receives an order request.
2. Validates the customer through Customer Service.
3. Retrieves product information from Product Service.
4. Checks product availability.
5. Reserves the required product stock.
6. Saves the order and its items in the Order database.
7. Returns the created order.

When a pending order is cancelled, the reserved product stock is released back to the Product Service.

## API Documentation

Swagger / OpenAPI documentation is available for exploring and testing the APIs.

**Central Swagger UI:**

`http://localhost:8080/swagger-ui/index.html`

The central Swagger UI provides access to:

- Customer Service API documentation
- Product Service API documentation
- Order Service API documentation
- Authentication Service API documentation

## Prerequisites

Before running the application, install:

- Java 21
- PostgreSQL
- Visual Studio Code or another Java IDE
- Git
- Postman (optional, for API testing)

## Database Setup

Create the following PostgreSQL databases:

```sql
CREATE DATABASE customer_db;
CREATE DATABASE product_db;
CREATE DATABASE order_db;
CREATE DATABASE auth_db;
```

Each service connects to its own database.

## Environment Configuration

The project uses a root `.env` file to configure database connections and JWT authentication.

The required environment variables are:

```properties
DB_USERNAME=your_postgresql_username
DB_PASSWORD=your_postgresql_password

CUSTOMER_DB_URL=jdbc:postgresql://localhost:5432/customer_db
PRODUCT_DB_URL=jdbc:postgresql://localhost:5432/product_db
ORDER_DB_URL=jdbc:postgresql://localhost:5432/order_db
AUTH_DB_URL=jdbc:postgresql://localhost:5432/auth_db

JWT_SECRET=your_base64_jwt_secret
```

To configure the project:

1. Copy `.env.example` to a new file named `.env` in the project root.
2. Replace the placeholder database username and password with your local PostgreSQL credentials.
3. Configure a secure Base64-encoded JWT secret.
4. Save the file.

**Important:** Never commit the `.env` file, database credentials or JWT secret to GitHub. The `.env` file is excluded using `.gitignore`.

## Running the Application

Start PostgreSQL first and ensure all four databases have been created.

Open five separate terminals and run each service using its Maven Wrapper.

### Customer Service

```powershell
cd D:\Microservices\customer-service
.\mvnw.cmd spring-boot:run
```

### Product Service

```powershell
cd D:\Microservices\product-service
.\mvnw.cmd spring-boot:run
```

### Order Service

```powershell
cd D:\Microservices\order-service
.\mvnw.cmd spring-boot:run
```

### Authentication Service

```powershell
cd D:\Microservices\auth-service
.\mvnw.cmd spring-boot:run
```

### API Gateway

```powershell
cd D:\Microservices\api-gateway
.\mvnw.cmd spring-boot:run
```

Ensure all required environment variables are configured before starting the services.

Once all services are running, access the application through:

`http://localhost:8080`

## Testing

The APIs have been manually tested using Postman, while automated testing has been implemented using JUnit 5 and Mockito to verify the business logic of the individual microservices.

The application has been checked for:

- Customer management operations, including creation, retrieval, updating, deletion and searching.
- Product management, including product validation, retrieval, updating, deletion and stock validation.
- Order creation, retrieval and cancellation.
- Product stock reservation and release during order processing.
- User registration, duplicate email validation, password encryption and authentication.
- JWT-protected API access through the API Gateway.
- Automated unit testing of Customer, Product, Authentication and Order Services.
- Error handling and validation for invalid requests and business rules.
- Centralised Swagger API documentation.

The automated tests use Mockito to isolate service dependencies and verify expected behaviour under both successful and failure scenarios. Maven is used to execute the test suites and report their results.

## Future Improvements

Although the core microservices functionality and automated testing have been implemented, several improvements could further enhance the application:

- Docker containerisation.
- CI / CD
- Service discovery.
- Distributed tracing and centralised monitoring.
- Message-driven communication between services.
- Circuit breaker and fault-tolerance mechanisms.

## Author

**Yash Chudgar**

E-Commerce Order Management System Using Spring Boot Microservices.
