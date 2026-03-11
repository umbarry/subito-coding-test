# Subito Coding Test

This project is a simplified version of a purchase cart service. It provides a RESTful API for managing products, baskets, and orders.

## Technologies Used

*   **Java:** 17
*   **Spring Boot:** 4.0.3
*   **Spring Data JPA:** For database access
*   **PostgreSQL:** As the primary database
*   **Kafka:** For event-driven communication
*   **Flyway:** For database migrations
*   **Lombok:** To reduce boilerplate code
*   **JWT:** For secure communication with the payment webhook
*   **MailHog:** For testing email sending
*   **Docker:** For containerization

## Requirements for Running

To run this project, you need to have Docker and Docker Compose installed on your machine.

## Running the Project

1.  Clone the repository:
    ```bash
    git clone https://github.com/your-username/subito-coding-test.git
    ```
2.  Navigate to the project directory:
    ```bash
    cd subito-coding-test
    ```
3.  Build and run the application using Docker Compose:
    ```bash
    cd scripts && sh run.sh
    ```
The application will be available at `http://localhost:8080`.

## OpenAPI Documentation

The OpenAPI documentation is available at the following link:

[openapi.yml](src/main/resources/openapi.yml)

You can also access the Swagger UI at `http://localhost:8080/swagger-ui.html`.

## Basket Management and Order Inserting

The API provides endpoints for managing baskets and creating orders.

*   **Basket Management:** You can create a basket for a user, add items to it, and remove items from it. A basket is considered `PENDING` until an order is created from it.
*   **Order Inserting:** You can create an order from a `PENDING` basket. When an order is created, the basket is marked as `COMPLETED`, and the product availability is updated.

## Payment Management with Webhook

Payment is managed asynchronously via a webhook. When an order is created, it has the status `INSERTED`. To simulate a payment, you can use the payment webhook.

The webhook expects a JWT in a query string parameter called `token`. The JWT should contain the following claims:

*   `sub`: The user identifier
*   `paymentId`: A unique identifier for the payment.
*   `paymentAccepted`: A boolean indicating whether the payment was accepted or rejected.

Here is an example of a JWT header amd payload:

**header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}

```
**payload:**
```json
{
  "sub": "user123",
  "paymentId": "pay_abc123XYZ789",
  "orderId": 1,
  "paymentAccepted": true,
  "iat": 1773183929,
  "exp": 1804719870
}
```

You can create a jwt using https://www.jwt.io tool with `subitoisthebest_subitoisthebest_`
as secret (defined as **environment variable** in `docker-compose.yml`)
or use the following:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMTIzIiwicGF5bWVudElkIjoicGF5X2FiYzEyM1hZWjc4OSIsIm9yZGVySWQiOjEsInBheW1lbnRBY2NlcHRlZCI6dHJ1ZSwiaWF0IjoxNzczMTgzOTI5LCJleHAiOjE4MDQ3MTk4NzB9.6ffO1jZk4v7K47Foj-HonbdUqbzLqjOFVhEby5KqTZc
```


When the webhook receives a valid JWT, it updates the order status to `PAID` if the payment was accepted.

## Expired Orders Cron Task

A scheduled task runs every minute to expire unpaid orders. An order is considered unpaid if it has the status `INSERTED` and is older than a configurable amount of time (default is 1 hour).

When an order expires, the following actions are taken:

*   The order status is updated to `EXPIRED`.
*   The **product availability is restored**.
*   A Kafka event is sent to **notify the user** that their order has expired.

## Kafka Events and Email Notifications

The application uses Kafka for event-driven communication. When a payment is successfully processed or an order expires, a Kafka event is sent. A Kafka consumer listens for these events and sends an email notification to the user.

You can view the emails sent by the application in MailHog at `http://localhost:8025`.

### Idempotency

Both the payment webhook and the Kafka consumers are idempotent.

*   **Payment Webhook:** The webhook checks for an existing payment with the same `paymentId` before processing a new one. This prevents duplicate processing.
*   **Kafka Consumers:** The consumers use optimistic locking to prevent duplicate processing of the same event. The `notificationSent` flag on the `Payment` and `Order` entities is updated before sending the email. If the update fails due to a concurrent update, the email is not sent.

## Ship Endpoint

The API provides an endpoint for shipping an order. When an order is shipped, the order status is updated to `SHIPPED`, and a tracking URL is associated with the order.

## Running tests

1.  Build and run the tests using docker:
    ```bash
    cd scripts && sh tests.sh
    ```