# Subito Order Service

A RESTful order management service built with Spring Boot, JPA/Hibernate, and PostgreSQL. This service allows users to create orders from a set of products and retrieve detailed order information including pricing and VAT calculations.

## Features

- Create orders with multiple items
- Calculate total price, VAT, and grand total
- View detailed item breakdown with individual prices and VAT
- Persistent data storage with PostgreSQL
- Full Docker support with Docker Compose
- Comprehensive test suite with H2 in-memory database

## Project Structure

```
subito-coding-test/
├── src/
│   ├── main/
│   │   ├── java/com/subito/subitocodingtest/
│   │   │   ├── model/          # Entity models (Order, OrderItem, Product)
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── repository/     # JPA repositories
│   │   │   ├── service/        # Business logic
│   │   │   ├── controller/     # REST endpoints
│   │   │   └── SubitoCodingTestApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/subito/subitocodingtest/
│       │   └── SubitoCodingTestApplicationTests.java
│       └── resources/
│           └── application-test.properties
├── scripts/
│   ├── tests.sh       # Run tests in Docker
│   └── run.sh         # Start the service with Docker Compose
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Database Schema

### Products Table
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(19,2) NOT NULL,
    vat_percentage NUMERIC(19,2) NOT NULL
);
```

### Orders Table
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY
);
```

### Order Items Table
```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    price NUMERIC(19,2) NOT NULL,
    vat NUMERIC(19,2) NOT NULL
);
```

## Prerequisites

- Docker and Docker Compose
- OR Java 17+ and Maven 3.9.6+

## Quick Start with Docker

### Start the Service

```bash
./scripts/run.sh
```

This will:
1. Build the Docker image
2. Start PostgreSQL database
3. Start the Spring Boot application
4. Database will be automatically initialized with schema

The API will be available at `http://localhost:8080`

### Run Tests in Docker

```bash
./scripts/tests.sh
```

This will:
1. Build the Docker image
2. Run the complete test suite
3. Report test results

### Stop the Service

```bash
docker-compose down
```

## Running Locally (without Docker)

### Prerequisites
- PostgreSQL running on localhost:5432
- Database: `subito_db`
- User: `postgres`
- Password: `postgres`

### Build and Run

```bash
mvn clean package
mvn spring-boot:run
```

### Run Tests

```bash
mvn test
```

## API Endpoints

### Create Order

**POST** `/api/orders`

Creates a new order with specified items.

**Request Example:**
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```

**Response Example (201 Created):**
```json
{
  "id": 1,
  "totalPrice": 2050.00,
  "totalVat": 451.00,
  "grandTotal": 2501.00,
  "items": [
    {
      "productId": 1,
      "productName": "Laptop",
      "quantity": 2,
      "unitPrice": 1000.00,
      "price": 2000.00,
      "vat": 440.00,
      "total": 2440.00
    },
    {
      "productId": 2,
      "productName": "Mouse",
      "quantity": 1,
      "unitPrice": 25.00,
      "price": 25.00,
      "vat": 5.50,
      "total": 30.50
    }
  ]
}
```

### Get Order

**GET** `/api/orders/{orderId}`

Retrieves the details of an existing order.

**Response Example (200 OK):**
Same format as Create Order response.

## Testing the API

### Using curl

```bash
# Create an order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"items":[{"productId":1,"quantity":2}]}'

# Get an order (replace {orderId} with actual ID)
curl http://localhost:8080/api/orders/1
```

### Using httpie

```bash
# Create an order
http POST http://localhost:8080/api/orders items:='[{"productId":1,"quantity":2}]'

# Get an order
http http://localhost:8080/api/orders/1
```

## Architecture

### Model Layer
- **Product**: Represents a product with price and VAT percentage
- **Order**: Represents an order containing multiple order items
- **OrderItem**: Represents an item in an order with quantity and calculated price/VAT

### Service Layer
- **OrderService**: Handles business logic for order creation and retrieval
  - Validates products exist
  - Calculates totals and VAT
  - Manages order persistence

### Repository Layer
- **OrderRepository**: JPA repository for Order entity
- **ProductRepository**: JPA repository for Product entity

### Controller Layer
- **OrderController**: REST endpoints for order operations

## Configuration

### Application Properties (PostgreSQL)
Located at `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:postgresql://db:5432/subito_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Test Configuration (H2 In-Memory)
Located at `src/test/resources/application-test.properties`

Uses H2 in-memory database for fast, isolated testing without requiring PostgreSQL.

## Dependencies

- **Spring Boot 4.0.3**: Web framework
- **Spring Data JPA**: ORM abstraction
- **Hibernate**: JPA implementation
- **PostgreSQL Driver**: Database connectivity
- **Lombok**: Reduce boilerplate code
- **H2 Database**: In-memory database for testing

## Error Handling

- **400 Bad Request**: Invalid product ID or missing required fields
- **404 Not Found**: Order or product not found
- **201 Created**: Successfully created order
- **200 OK**: Successfully retrieved order

## Development

### Project Structure Best Practices

- Clean separation of concerns (Model, DTO, Service, Repository, Controller)
- Dependency injection via constructor
- Transaction management with `@Transactional`
- Proper entity lifecycle management

### Testing Strategy

- Unit tests with embedded H2 database
- Integration tests using Spring Boot Test context
- Comprehensive test coverage for order calculations
- Error scenarios covered

## Troubleshooting

### Database Connection Issues

Ensure PostgreSQL is running and accessible:
```bash
docker-compose logs db
```

### Port Conflicts

If port 8080 is already in use, modify `docker-compose.yml`:
```yaml
ports:
  - "8081:8080"  # Maps localhost:8081 to container:8080
```

### Build Issues

Clean and rebuild:
```bash
mvn clean
mvn build
```

## Future Enhancements

- Product search and filtering
- Order status tracking
- Order update and cancellation
- User authentication and authorization
- API documentation with Swagger/OpenAPI
- Batch order operations
- Order history and analytics

## License

Simplified version of a purchase cart service for Subito coding test.
