# E-Commerce API

Production-grade monolithic e-commerce backend built with Java 21, Spring Boot 3.x, MySQL, JWT security, MapStruct, JavaMailSender, and Thymeleaf.

## Modules

- User: register, get, update, delete, BCrypt password storage.
- Product: create, update, deactivate, get, list.
- Inventory: create inventory, update stock, reserve stock, release stock with optimistic locking.
- Order: place, cancel, get, get by user, idempotency key support.
- Email: async HTML email sending with success/failure logs.
- Notification: persistent business notifications.

## Local Setup

1. Install Java 21, Maven 3.9+, and MySQL 8.
2. Create a database user or adjust `.env` from `.env.example`.
3. Copy `.env.example` to `.env` and replace secrets.
4. Start MySQL and run:

```bash
mvn spring-boot:run
```

Hibernate is configured with `ddl-auto=update` for local development, and a standalone SQL script is available at `database/schema.sql`.

Seeded test users are created when `DATA_SEEDER_ENABLED=true`:

- Admin: `admin@example.com` / `Password123!`
- Customer: `customer@example.com` / `Password123!`

## API Docs

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## Authentication

Register or login through `/api/v1/auth/**`. Use the returned JWT:

```http
Authorization: Bearer {{jwtToken}}
```

Roles:

- `ADMIN`: product, inventory, and cross-user operations.
- `CUSTOMER`: own users/orders/notifications.

## Request Format

```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {}
}
```

Validation errors return:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "must be a well-formed email address"
    }
  ]
}
```

## Production Deployment Guide

- Provide secrets via environment variables or mounted `.env`; never commit real values.
- Use a strong `JWT_SECRET` of at least 256 bits.
- Prefer explicit SQL migrations or managed database change scripts for production schema changes.
- Put the app behind TLS and a reverse proxy or load balancer.
- Use a managed MySQL instance with backups, slow query logging, and connection limits.
- Ship logs to a central system and preserve `X-Correlation-Id`.
- Configure a production SMTP provider and monitor `email_failure_total`.

## Important Files

- `src/main/java/com/example/ecommerce`: complete source code.
- `database/schema.sql`: full database script with indexes and constraints.
- `docs/architecture.md`: architecture diagram and design decisions.
- `docs/er-diagram.md`: ER diagram.
- `docs/sequence-diagrams.md`: business flow sequence diagrams.
- `postman/ecommerce-api.postman_collection.json`: Postman collection.
- `http/*.http`: IntelliJ HTTP Client examples.
- `.env.example`: safe environment variable template.
