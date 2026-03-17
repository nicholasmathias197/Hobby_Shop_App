# Gundam Hobby Shop — Full Stack E-Commerce Application

A full-stack e-commerce platform for Gundam models and modeling supplies. Built with Spring Boot 4 and React 18, deployed on AWS using Terraform and CodePipeline.

**Live Frontend:** http://gundam-hobby-shop-frontend-911784620581.s3-website.us-east-2.amazonaws.com  
**API Base URL:** http://18.222.156.219:8080

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Tech Stack](#tech-stack)
- [Local Setup](#local-setup)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [AWS Deployment](#aws-deployment)
- [CI/CD Pipeline](#cicd-pipeline)
- [Testing](#testing)
- [Architecture Decisions](#architecture-decisions)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                        Client                           │
│         React 18 + Vite  →  S3 Static Website           │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTP (port 8080)
┌──────────────────────────▼──────────────────────────────┐
│                    EC2 t3.micro                          │
│         Spring Boot 4 JAR  +  MariaDB (local)           │
│         IAM Role: S3ReadOnly + SSMManagedInstanceCore    │
└─────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                    S3 Bucket                            │
│   Frontend assets  |  App JAR  |  SQL dump  |  TF state │
└─────────────────────────────────────────────────────────┘
```

**Key design choices:**
- Single EC2 instance hosts both the Spring Boot API and MariaDB to stay within free-tier/capstone budget
- Frontend is a static React SPA served from S3 — no server needed
- Terraform state stored in the same S3 bucket under key `tfstate`
- EC2 pulls the JAR and SQL dump from S3 on first boot via `user_data`

---

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Backend | Spring Boot | 4.0.0-RC2 |
| Language | Java | 21 LTS |
| Security | Spring Security + JWT (jjwt) | 0.12.6 |
| ORM | Spring Data JPA / Hibernate | 7.x |
| Database | MariaDB | 10.5 |
| Mapping | MapStruct | 1.6.3 |
| Build | Maven | 3.9.x |
| Frontend | React | 18.2.0 |
| Bundler | Vite | 7.x |
| HTTP Client | Axios | 1.x |
| Routing | React Router DOM | 6.x |
| IaC | Terraform | >= 1.0 |
| CI/CD | AWS CodePipeline + CodeBuild | - |
| Code Quality | SonarCloud + JaCoCo | - |

---

## Local Setup

### Prerequisites

- Java 21
- Maven 3.9+
- Node.js 20+ and npm
- MariaDB or MySQL 8.0+

### Backend

```bash
# 1. Clone the repo
git clone <repository-url>
cd Hobby_Shop_App

# 2. Create the database
mysql -u root -p
CREATE DATABASE hobby_shop_db;
exit;

# 3. Load the schema and seed data
mysql -u root -p hobby_shop_db < Server/src/main/resources/hobbyshop.sql

# 4. Configure credentials (or use application-dev.properties)
# Edit Server/src/main/resources/application.properties:
#   spring.datasource.username=<your_db_user>
#   spring.datasource.password=<your_db_password>

# 5. Build and run
cd Server
mvn spring-boot:run
# API available at http://localhost:8080
```

### Frontend

```bash
cd Client/hobby-shop-frontend
npm install
npm run dev
# App available at http://localhost:5173
```

> The frontend proxies API calls to `http://localhost:8080` in dev mode. See `vite.config.js` for proxy configuration.

---

## API Documentation

All endpoints are prefixed with `/api`. A full Postman collection is included at `Hobby Shop.postman_collection.json`.

### Authentication — `/api/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Register a new customer account |
| POST | `/api/auth/login` | Public | Login and receive a JWT token |

**Login request body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Login response:**
```json
{
  "token": "<jwt_token>",
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "roles": ["ROLE_USER"]
}
```

Pass the token in subsequent requests as: `Authorization: Bearer <jwt_token>`

Cart merging: include `X-Session-ID` header on login/register to merge a guest cart into the authenticated user's cart.

---

### Products — `/api/products`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/products` | Public | Paginated list of all active products |
| GET | `/api/products/{id}` | Public | Get product by ID |
| GET | `/api/products/sku/{sku}` | Public | Get product by SKU |
| GET | `/api/products/featured` | Public | Paginated featured products |
| GET | `/api/products/brand/{brandId}` | Public | Products by brand |
| GET | `/api/products/category/{categoryId}` | Public | Products by category |
| GET | `/api/products/search?q={term}` | Public | Full-text search |
| GET | `/api/products/filter` | Public | Multi-criteria filter (see below) |
| POST | `/api/products` | Admin | Create product |
| PUT | `/api/products/{id}` | Admin | Update product |
| DELETE | `/api/products/{id}` | Admin | Soft-delete product |
| PUT | `/api/products/{id}/restore` | Admin | Restore soft-deleted product |
| GET | `/api/products/admin/all` | Admin | All products including inactive |
| GET | `/api/products/count/active` | Admin | Count of active products |
| GET | `/api/products/count/inactive` | Admin | Count of inactive products |
| GET | `/api/products/count/featured` | Admin | Count of featured products |

**Filter query params** (`GET /api/products/filter`):

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `categoryId` | Long | No | Filter by category |
| `brandId` | Long | No | Filter by brand |
| `minPrice` | BigDecimal | No | Minimum price |
| `maxPrice` | BigDecimal | No | Maximum price |
| `searchTerm` | String | No | Keyword search |

All list endpoints support Spring pagination params: `page`, `size`, `sort`.

---

### Orders — `/api/orders`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/orders` | User | Create order (authenticated) |
| POST | `/api/orders/guest` | Public | Create guest order (requires `X-Session-ID`) |
| GET | `/api/orders` | User | Get current user's orders |
| GET | `/api/orders/{orderNumber}` | User | Get specific order |
| PUT | `/api/orders/{orderId}/cancel` | User | Cancel an order |
| GET | `/api/orders/guest/lookup` | Public | Guest order lookup by email + order number |
| GET | `/api/orders/all` | Admin | All orders |
| GET | `/api/orders/status/{status}` | Admin | Orders by status |
| PUT | `/api/orders/{orderId}/status` | Admin | Update order status |
| PUT | `/api/orders/{orderId}/payment` | Admin | Update payment status |
| PUT | `/api/orders/{orderId}/tracking` | Admin | Update tracking number |

---

### Cart — `/api/cart`

Supports both authenticated users (JWT) and guests (session ID via `X-Session-ID` header).

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/cart` | User/Guest | Get current cart |
| POST | `/api/cart/items` | User/Guest | Add item to cart |
| PUT | `/api/cart/items/{itemId}` | User/Guest | Update item quantity |
| DELETE | `/api/cart/items/{itemId}` | User/Guest | Remove item from cart |
| DELETE | `/api/cart` | User/Guest | Clear cart |

---

### Other Endpoints

| Prefix | Description |
|--------|-------------|
| `/api/categories` | CRUD for product categories (admin write, public read) |
| `/api/brands` | CRUD for brands (admin write, public read) |
| `/api/reviews` | Product reviews (authenticated users) |
| `/api/customers` | Customer management (admin) |
| `/api/health` | Health check — returns `200 OK` |

---

## Database Schema

The database is MariaDB running locally on the EC2 instance. Schema is loaded from `Server/src/main/resources/hobbyshop.sql`.

### Core Tables

| Table | Description |
|-------|-------------|
| `customers` | User accounts with roles (USER, ADMIN) |
| `products` | Product catalog with soft-delete support |
| `brands` | Product brands (Bandai, Tamiya, etc.) with `logo_url` |
| `categories` | Product categories |
| `orders` | Order headers with status tracking |
| `order_items` | Line items per order |
| `carts` | Persistent carts (user and guest) |
| `cart_items` | Items within a cart |
| `reviews` | Customer product reviews |

### Key Relationships

```
customers ──< orders ──< order_items >── products
customers ──< carts  ──< cart_items  >── products
products  >── brands
products  >── categories
products  ──< reviews >── customers
```

### Notable Design Choices

- Products use soft-delete (`active` boolean) — deleted products remain in the DB for order history integrity
- Carts support both authenticated users (linked by `customer_id`) and guests (linked by `session_id`)
- Orders store a snapshot of the shipping address at time of purchase
- `order_items` stores `unit_price` at time of purchase to preserve historical pricing

---

## AWS Deployment

### Infrastructure (Terraform)

All infrastructure is defined in `main.tf` and managed via Terraform with remote state in S3.

| Resource | Details |
|----------|---------|
| EC2 Instance | `t3.micro`, Amazon Linux 2023, `ami-0b0b78dcacbab728f` |
| Security Group | Inbound: 22 (SSH), 8080 (API). Outbound: all |
| IAM Role | `AmazonS3ReadOnlyAccess` + `AmazonSSMManagedInstanceCore` |
| S3 Bucket | Static website hosting + artifact storage |
| Terraform State | S3 key: `tfstate` |

### Manual Deployment

```bash
# Initialize Terraform (first time only)
terraform init

# Preview changes
terraform plan

# Apply infrastructure
terraform apply
```

### EC2 First Boot

On first launch, `user_data` automatically:
1. Installs Java 21 (Amazon Corretto) and MariaDB
2. Creates the `hobby_shop_db` database
3. Pulls `hobbyshop.sql` and the app JAR from S3
4. Loads the schema and starts the Spring Boot service via systemd

To manually restart the app on EC2 via SSM:
```bash
aws ssm send-command \
  --instance-ids i-024e937074f792f29 \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=["sudo systemctl restart hobby-shop"]' \
  --region us-east-2
```

---

## CI/CD Pipeline

**Pipeline name:** `TerraformDeployToAWS`  
**Stages:** Source → Code-Analysis → Build

| Stage | CodeBuild Project | Description |
|-------|------------------|-------------|
| Source | — | GitHub webhook trigger |
| Code-Analysis | `U197_Hobbies_SonarCloud_Analysis` | Runs JaCoCo + SonarCloud scan |
| Build | `U197_Hobbies_Capstone_Project` | Builds JAR + React, uploads to S3, runs Terraform, restarts EC2 |

### Build (`buildspec.yml`)

- Installs Java 21, Node 20, Maven, Terraform via `dnf`
- Runs `mvn clean package` to produce the JAR
- Runs `npm ci && npm run build` for the React app
- Uploads JAR and SQL to `s3://.../app/`
- Syncs React build to S3 root for static hosting
- Runs `terraform apply -auto-approve` with dynamic imports
- Triggers `scripts/ssm-restart.sh` to restart the EC2 service

### SonarCloud (`buildspec-sonar.yml`)

- Runs `mvn clean package jacoco:report` to generate coverage data
- Runs `mvn sonar:sonar` pointing at JaCoCo XML report
- Non-blocking (`|| true`) — scan results are informational and won't fail the pipeline

### SonarCloud Configuration

- **Organization:** `nicholasmathias197`
- **Project Key:** `nicholasmathias197_Hobby_Shop_App`
- **Token:** Stored in AWS SSM Parameter Store at `/sonarcloud/token`

---

## Testing

```bash
cd Server

# Run all tests
mvn test

# Run tests with coverage report
mvn clean package jacoco:report

# Coverage report location
open target/site/jacoco/index.html
```

Tests use JUnit 5, Mockito 5, and an H2 in-memory database for repository tests. JaCoCo is configured to generate an XML report consumed by SonarCloud for coverage tracking.

---

## Architecture Decisions

**Why Spring Boot 4.0.0-RC2?**  
Chosen to use the latest Spring Boot release for capstone. Uses Hibernate 7 and Jakarta EE 10 namespaces. `MySQLDialect` replaces the deprecated `MySQL8Dialect`.

**Why MariaDB on EC2 instead of RDS?**  
Keeps the project within a single EC2 instance to minimize AWS costs. MariaDB is fully compatible with the MySQL JDBC driver used by Spring.

**Why S3 for frontend hosting?**  
Zero server cost for static assets. React Router is handled by setting the S3 error document to `index.html` so client-side routing works correctly.

**Why JWT over sessions?**  
Stateless authentication fits the REST API model and works cleanly with the S3-hosted SPA — no shared session store needed.

**Why useReducer in ProductsPage?**  
The products page manages 6+ related state values (filters, pagination, loading, error, data). `useReducer` consolidates these into a single state object with predictable transitions, making the component easier to reason about.

**Why useMemo/useCallback in CartProvider?**  
The cart context is consumed by many components. Memoizing derived values (`cartTotal`, `cartItemsCount`) and stabilizing function references prevents unnecessary re-renders across the component tree.
