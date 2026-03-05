# Subito Coding Test - Purchase Cart Service

A comprehensive RESTful web service for managing orders with a shopping basket system, built with Spring Boot, JPA/Hibernate, PostgreSQL, and Docker.

## Features Implemented

### ✅ Core Features

1. **Product Management**
   - Create, read, update, and delete products
   - Track product availability with `availableItems`
   - Store net price (renamed from `price`) and VAT percentage
   - Optimistic locking support with `@Version` field

2. **Basket Management**
   - Create and manage shopping baskets
   - Add/remove items from basket
   - Track total price, VAT, and grand total for basket items

3. **Order Management**
   - Create orders from basket with user and shipping information
   - Track order status (INSERTED by default)
   - Automatically update product availability when order is created
   - Store order insert date (set automatically with `@PrePersist`)
   - Calculate totals: total price, total VAT, and grand total

4. **User & Shipping Information**
   - Store user details: first name, last name, email, phone number
   - Store shipping address: street, city, postal code, country
   - Both validated using Hibernate Validator

5. **Global Exception Handling**
   - `@ControllerAdvice` for centralized exception handling
   - `ResourceNotFoundException` with enum-based resource types (ORDER, PRODUCT, BASKET)
   - Validation exception handling for request validation
   - Consistent error response format with HTTP status codes

6. **Data Validation**
   - Hibernate Validator annotations on all entities
   - Request DTO validation with `@Valid`
   - Custom validation messages

7. **Optimistic Locking**
   - `@Version` field on Product entity
   - Prevents concurrent update conflicts when reducing available items

## Project Structure

```
src/main/java/com/subito/subitocodingtest/
├── controller/
│   ├── OrderController.java
│   ├── ProductController.java
│   └── BasketController.java
├── service/
│   ├── OrderService.java
│   ├── ProductService.java
│   └── BasketService.java
├── model/
│   ├── Order.java
│   ├── OrderItem.java
│   ├── Basket.java
│   ├── BasketItem.java
│   ├── Product.java
│   ├── UserInfo.java
│   ├── ShippingInfo.java
│   └── OrderStatus.java (enum)
├── dto/
│   ├── OrderResponse.java & CreateOrderRequest.java
│   ├── ProductResponse.java & ProductRequest.java
│   ├── BasketResponse.java & BasketItemRequest.java
│   ├── BasketItemResponse.java
│   ├── UserInfoRequest.java & UserInfoResponse.java
│   └── ShippingInfoRequest.java & ShippingInfoResponse.java
├── repository/
│   ├── OrderRepository.java
│   ├── ProductRepository.java
│   ├── BasketRepository.java
│   ├── UserInfoRepository.java
│   └── ShippingInfoRepository.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    ├── ResourceType.java (enum)
    └── ErrorResponse.java
```

## API Endpoints

### Products
- `POST /api/products` - Create a new product
- `GET /api/products` - Get all products (paginated)
- `GET /api/products/{productId}` - Get a specific product
- `PUT /api/products/{productId}` - Update a product
- `DELETE /api/products/{productId}` - Delete a product

### Baskets
- `POST /api/baskets` - Create a new basket
- `GET /api/baskets/{basketId}` - Get a basket
- `POST /api/baskets/{basketId}/items` - Add item to basket
- `DELETE /api/baskets/{basketId}/items/{basketItemId}` - Remove item from basket
- `DELETE /api/baskets/{basketId}` - Delete basket

### Orders
- `POST /api/orders` - Create an order
- `GET /api/orders/{orderId}` - Get order details

## Example Order Creation Request

```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "userInfo": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phoneNumber": "1234567890"
  },
  "shippingInfo": {
    "street": "123 Main St",
    "city": "New York",
    "postalCode": "10001",
    "country": "USA"
  }
}
```

## Example Order Response

```json
{
  "id": 1,
  "status": "INSERTED",
  "insertDate": "2026-03-05T21:40:00",
  "userInfo": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phoneNumber": "1234567890"
  },
  "shippingInfo": {
    "id": 1,
    "street": "123 Main St",
    "city": "New York",
    "postalCode": "10001",
    "country": "USA"
  },
  "totalPrice": 2000.00,
  "totalVat": 440.00,
  "grandTotal": 2440.00,
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "Laptop",
      "quantity": 2,
      "unitNetPrice": 1000.00,
      "price": 2000.00,
      "vat": 440.00,
      "total": 2440.00
    }
  ]
}
```

## Error Handling

All errors return a consistent response format:

```json
{
  "status": 404,
  "message": "Order not found with id: 999",
  "error": "Order Not Found",
  "timestamp": "2026-03-05T21:40:00"
}
```

HTTP Status Codes:
- `201 Created` - Resource created successfully
- `200 OK` - Request successful
- `204 No Content` - Deletion successful
- `400 Bad Request` - Validation error
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Unexpected error

## Database

### Technology Stack
- **Database**: PostgreSQL
- **ORM**: Hibernate/JPA
- **Transactions**: Spring Transaction Management
- **Locking**: Optimistic locking with @Version

### Key Entities

**Product**
- Optimistic locking version field
- Available items tracking
- Net price (not including VAT)
- VAT percentage

**Order**
- Status (INSERTED, PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- Insert date (set at creation)
- User info reference
- Shipping info reference
- Order items collection

**Basket**
- Items collection
- Calculated totals

## Docker Setup

The project includes Docker configuration for PostgreSQL database.

### Running with Docker Compose

```bash
docker-compose up -d
```

This will start a PostgreSQL instance on port 5432.

### Running Tests with Docker

```bash
bash scripts/tests.sh
```

### Running Service with Docker

```bash
bash scripts/run.sh
```

## Building and Testing

### Build the project
```bash
mvn clean package
```

### Run tests
```bash
mvn test
```

### Run the application
```bash
mvn spring-boot:run
```

## Testing

The project includes 6 comprehensive unit tests:
- ✅ `testCreateOrder` - Verify order creation with multiple items
- ✅ `testOrderCalculations` - Verify price and VAT calculations
- ✅ `testGetOrder` - Verify order retrieval
- ✅ `testOrderNotFound` - Verify 404 error handling
- ✅ `testProductNotFound` - Verify product validation
- ✅ `contextLoads` - Verify Spring context loads

All tests use:
- `@SpringBootTest` with test profile
- H2 in-memory database
- Transactional test management

## Dependencies

### Core
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- Lombok

### Database
- PostgreSQL Driver
- H2 Database (test)

### Build
- Maven Compiler Plugin
- Maven Surefire Plugin (tests)
- Spring Boot Maven Plugin

## Validation Rules

### Product
- Name: Not blank
- Net Price: Not null, positive or zero
- VAT Percentage: Not null, positive or zero
- Available Items: Not null, positive or zero

### UserInfo
- First Name: Not blank
- Last Name: Not blank
- Email: Valid email format, unique
- Phone Number: Not blank

### ShippingInfo
- Street: Not blank
- City: Not blank
- Postal Code: Not blank
- Country: Not blank

### OrderItem
- Product: Not null
- Quantity: Not null, must be positive (> 0)

## Key Implementation Details

### Optimistic Locking
When creating an order, product availability is updated using optimistic locking. This prevents race conditions when multiple orders try to reduce the same product's availability simultaneously.

```java
@Version
private Long version;
```

### Auto-generated Fields
- Order insert date is automatically set via `@PrePersist`
- Order status defaults to INSERTED
- Product version field for optimistic locking

### Price Calculations
- Unit Net Price: Product's net price
- Item Price: Unit Net Price × Quantity
- Item VAT: Item Price × (VAT Percentage / 100)
- Order Total Price: Sum of all item prices
- Order Total VAT: Sum of all item VATs

### Exception Handling
The `ResourceNotFoundException` uses a `ResourceType` enum to differentiate between resource types and generate appropriate error messages without creating separate exception classes.


