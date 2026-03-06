# Hobby Shop Backend API

Spring Boot REST API for an online hobby shop specializing in Gundam models and modeling supplies.

## Tech Stack

- Java 25
- Spring Boot 3.2.x
- Spring Security with JWT
- Spring Data JPA
- MySQL Database
- Maven
- Lombok
- MapStruct

## Features

- User authentication and authorization (JWT-based)
- Product catalog management
- Shopping cart functionality (supports both authenticated and guest users)
- Order processing
- Product search and filtering
- Category and brand management
- Product reviews and ratings

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Products
- `GET /api/products` - Get all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/sku/{sku}` - Get product by SKU
- `GET /api/products/search?query=` - Search products
- `GET /api/products/filter` - Filter products
- `POST /api/products` - Create product (Admin)
- `PUT /api/products/{id}` - Update product (Admin)
- `DELETE /api/products/{id}` - Deactivate product (Admin)

### Cart
- `GET /api/cart` - Get current cart
- `POST /api/cart/items` - Add item to cart
- `PUT /api/cart/items/{itemId}?quantity=` - Update cart item
- `DELETE /api/cart/items/{itemId}` - Remove item from cart
- `DELETE /api/cart/clear` - Clear cart

### Categories
- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `GET /api/categories/{id}/products` - Get products by category
- `POST /api/categories` - Create category (Admin)
- `PUT /api/categories/{id}` - Update category (Admin)
- `DELETE /api/categories/{id}` - Delete category (Admin)

### Brands
- `GET /api/brands` - Get all brands
- `GET /api/brands/{id}` - Get brand by ID
- `GET /api/brands/{id}/products` - Get products by brand
- `POST /api/brands` - Create brand (Admin)
- `PUT /api/brands/{id}` - Update brand (Admin)
- `DELETE /api/brands/{id}` - Delete brand (Admin)

### Orders
- `GET /api/orders` - Get user orders
- `GET /api/orders/{orderNumber}` - Get order by number
- `POST /api/orders` - Create order
- `PUT /api/orders/{id}/status` - Update order status (Admin)

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/hobby-shop-backend.git
   cd hobby-shop-backend