# 🛒 Mini E-Commerce Microservices

A production-ready e-commerce platform built with microservices architecture, Spring Boot, Docker, and comprehensive observability.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Microservices](#microservices)
- [Technology Stack](#technology-stack)
- [Observability](#observability)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Monitoring \u0026 Logging](#monitoring--logging)

## 🎯 Overview

This project demonstrates a production-ready microservices architecture for an e-commerce platform with full observability. Features include user authentication, product catalog, shopping cart, order processing, and comprehensive monitoring and logging.

### Key Features

✅ **Microservices Architecture** - Independent, scalable services  
✅ **API Gateway** - Single entry point with JWT authentication  
✅ **Message Queue** - Asynchronous communication via RabbitMQ  
✅ **Database per Service** - PostgreSQL for data isolation  
✅ **Metrics Monitoring** - Prometheus + Grafana dashboards  
✅ **Centralized Logging** - ELK Stack (Elasticsearch, Logstash, Kibana)  
✅ **Containerization** - Docker Compose orchestration  
✅ **Cart Management** - Shopping cart with automatic order creation

## 🏗️ Architecture

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
┌──────▼──────────┐
│  API Gateway    │ (JWT Auth)
│   Port: 8080    │
└────────┬────────┘
         │
    ┌────┴────┬─────────┬─────────┬──────────┐
    │         │         │         │          │
┌───▼───┐ ┌──▼──┐ ┌────▼────┐ ┌──▼───┐ ┌───▼────┐
│ Auth  │ │Prod │ │  Order  │ │ Cart │ │  File  │
│ 8080  │ │8080 │ │  8080   │ │ 8080 │ │  8080  │
└───┬───┘ └──┬──┘ └────┬────┘ └──┬───┘ └───┬────┘
    │        │         │         │          │
    └────────┴─────────┴─────────┴──────────┘
                       │
                  ┌────▼─────┐
                  │ RabbitMQ │
                  └──────────┘
```

### Observability Stack

```
Metrics:  Prometheus (9090) → Grafana (3001)
Logs:     Services → Logstash (5001) → Elasticsearch (9200) → Kibana (5601)
```

## 🔧 Microservices

| Service             | Description                           | Port | Database   | Features                 |
| ------------------- | ------------------------------------- | ---- | ---------- | ------------------------ |
| **Auth Service**    | User authentication \u0026 JWT tokens | 8080 | PostgreSQL | Registration, Login, JWT |
| **Product Service** | Product catalog \u0026 management     | 8080 | PostgreSQL | CRUD, Search, Categories |
| **Order Service**   | Order \u0026 cart management          | 8080 | PostgreSQL | Cart, Orders, Stock sync |
| **Gateway Service** | API Gateway \u0026 routing            | 8080 | -          | JWT validation, Routing  |
| **File Service**    | File upload \u0026 storage            | 8080 | -          | Image uploads            |

## 🛠️ Technology Stack

### Backend

- **Framework**: Spring Boot 3.2.3 / 4.0.0
- **Language**: Java 21
- **Build Tool**: Maven
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 16
- **ORM**: Spring Data JPA
- **Message Queue**: RabbitMQ 3

### Observability

- **Metrics**: Prometheus + Grafana
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Monitoring**: Spring Boot Actuator + Micrometer

### DevOps

- **Containerization**: Docker
- **Orchestration**: Docker Compose
- **CI/CD**: Make + Docker

## 📊 Observability

### Metrics (Prometheus + Grafana)

**Access**: http://localhost:3001 (Grafana) | http://localhost:9090 (Prometheus)

**What you get:**

- HTTP request rates and response times
- JVM metrics (memory, CPU, threads, GC)
- Database connection pool stats
- Custom business metrics

**Pre-built Dashboards:**

- Spring Boot Statistics (ID: 11378)
- JVM Micrometer (ID: 4701)

### Logs (ELK Stack)

**Access**: http://localhost:5601 (Kibana) | http://localhost:9200 (Elasticsearch)

**What you get:**

- Centralized logs from all services
- Fast search and filtering
- Log correlation across services
- Error tracking and analysis

**Index Pattern**: `microservices-logs-*`

## 📦 Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **Docker \u0026 Docker Compose**
- **8GB+ RAM** (for ELK Stack)
- **Make** (optional, for build commands)

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/ibrahimGoumrane/mini-ecommerce-microservices.git
cd mini-ecommerce-microservices/microservices
```

### 2. Configure Environment

Create `.env` file in `microservices/` directory:

```env
# Database
DB_USER=postgres
DB_PASS=postgres
DB_NAME_AUTH=auth_db
DB_NAME_PRODUCT=product_db
DB_NAME_ORDER=order_db

# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USER=admin
RABBITMQ_PASS=admin

# JWT
JWT_SECRET=your-secret-key-here

# Services
PRODUCT_SERVICE_URL=http://product-service:8080
FILE_SERVICE_URL=http://file-service:8080
```

### 3. Start All Services

```bash
# Start infrastructure (database, message queue, monitoring)
docker-compose up -d postgres rabbitmq prometheus grafana elasticsearch logstash kibana

# Build and start microservices
make product-service
make order-service
make auth-service
make gateway-service
make file-service
```

Or start everything at once:

```bash
docker-compose up -d
```

### 4. Verify Deployment

```bash
# Check all containers are running
docker ps

# Check service health
curl http://localhost:8081/actuator/health  # Product service
curl http://localhost:8082/actuator/health  # Order service
```

## 🎯 Kubernetes Deployment (Alternative)

For Kubernetes deployment with Minikube, see detailed instructions in [docs/README.md](docs/README.md).

### Quick Start with Kubernetes

```bash
# Start Minikube
minikube start --cpus=4 --memory=6000

# Create namespace and configs
kubectl apply -f k8s/config/namespace.yaml
kubectl apply -f k8s/config/secrets.yaml -n microservices
kubectl apply -f k8s/config/configmap.yaml -n microservices

# Deploy all services (deployments + services)
kubectl apply -f k8s/services/ -n microservices
kubectl apply -f k8s/rabbitmq/ -n microservices

# Deploy logging stack (ELK)
kubectl apply -f k8s/logging/ -n microservices

# Deploy monitoring stack (Prometheus + Grafana)
kubectl apply -f k8s/monitoring/ -n microservices

# Deploy ingress
kubectl apply -f k8s/gateway/ingress.yaml -n microservices

# Check deployment
kubectl get pods -n microservices
kubectl get svc -n microservices
```

### Access Services in Kubernetes

```bash
# Kibana (Logging Dashboard)
minikube service kibana -n microservices
# Access at http://127.0.0.1:<tunnel-port>

# Grafana (Monitoring Dashboard)
minikube service grafana -n microservices
# Default credentials: admin / admin

# Prometheus (Metrics)
kubectl port-forward -n microservices svc/prometheus 9090:9090
# Access at http://localhost:9090

# Get Ingress IP for API access
minikube ip
# Access APIs at http://<minikube-ip>/api/v1/*
```

> **Note**: On Windows with Docker driver, use `minikube service` tunnels (http://127.0.0.1:port) instead of NodePort IPs. Keep terminal open while using tunnels.

## 📚 API Documentation

### Base URL

```
http://localhost:8080/api/v1
```

### Authentication

```bash
# Register
POST /api/v1/auth/register
{
  "username": "john",
  "email": "john@example.com",
  "password": "password123"
}

# Login
POST /api/v1/auth/login
{
  "username": "john",
  "password": "password123"
}
# Returns: { "token": "eyJhbGc..." }
```

### Products

```bash
# List products
GET /api/v1/products?page=1&limit=10

# Get product
GET /api/v1/products/{id}

# Create product (multipart/form-data)
POST /api/v1/products
- name: "Laptop"
- price: 999.99
- stockQuantity: 50
- category: "Electronics"
- mainImage: <file>
- description: "Optional description"
- secondaryImages: <files> (optional)
```

### Cart \u0026 Orders

```bash
# Add to cart
POST /api/v1/cart/add
{
  "userId": 1,
  "productId": 5,
  "quantity": 2
}

# View cart
GET /api/v1/cart/current?userId=1

# Create order from cart
POST /api/v1/orders
{
  "customerId": 1,
  "items": [],  # Empty = use cart items
  "shippingAddress": "123 Main St, City"
}
```

## 📊 Monitoring \u0026 Logging

### Access Dashboards

| Service        | URL                    | Credentials   |
| -------------- | ---------------------- | ------------- |
| **Grafana**    | http://localhost:3001  | admin / admin |
| **Prometheus** | http://localhost:9090  | -             |
| **Kibana**     | http://localhost:5601  | -             |
| **RabbitMQ**   | http://localhost:15672 | admin / admin |

### Grafana Setup

1. Login to http://localhost:3001
2. Go to Dashboards → Import
3. Enter dashboard ID: **11378** (Spring Boot)
4. Select Prometheus datasource
5. Repeat with ID: **4701** (JVM)

### Kibana Setup

1. Open http://localhost:5601
2. Go to Management → Index Patterns
3. Create pattern: `microservices-logs-*`
4. Select `@timestamp` as time field
5. View logs in Discover tab

### Useful Kibana Queries

```
# View errors only
level: "ERROR"

# Logs from specific service
service: "order-service"

# Search in messages
message: "order created"
```

## 🔄 Development Workflow

### Build Single Service

```bash
cd microservices
make product-service  # Builds and deploys product-service
```

### View Logs

```bash
# Service logs
docker logs microservices-product-service-1 -f

# All logs
docker-compose logs -f
```

### Rebuild After Changes

```bash
# Rebuild specific service
make product-service

# Rebuild all
docker-compose up -d --build
```

## 📁 Project Structure

```
mini-ecommerce-microservices/
├── microservices/
│   ├── auth-service/          # Authentication
│   ├── gateway-service/       # API Gateway
│   ├── product-service/       # Products
│   ├── order-service/         # Orders \u0026 Cart
│   ├── file-service/          # File uploads
│   ├── monitoring/            # Prometheus \u0026 Grafana configs
│   ├── logging/               # Logstash pipeline
│   └── docker-compose.yaml    # All services
├── docs/                      # Documentation
└── README.md
```

## 🎯 Key Features Explained

### Cart-Based Order Creation

Orders can be created directly from cart without passing items:

```json
POST /api/v1/orders
{
  "customerId": 1,
  "shippingAddress": "123 Main St"
}
```

The system automatically:

1. Retrieves items from user's cart
2. Creates order with cart items
3. Reserves stock via RabbitMQ
4. Clears the cart

### Stock Management

When an order is created:

1. Order service publishes `ProductStockUpdateEvent` to RabbitMQ
2. Product service listens and reserves stock
3. Stock quantity automatically decreases
4. Detailed logs track the entire flow

### Optional Fields

Product `description` and `secondaryImages` are optional - create minimal products quickly!

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📝 License

This project is licensed under the MIT License.

## 👤 Author

**Ibrahim Goumrane**

- GitHub: [@ibrahimGoumrane](https://github.com/ibrahimGoumrane)

## 🙏 Acknowledgments

- Spring Boot team
- Prometheus \u0026 Grafana communities
- Elastic (ELK Stack)
- All open-source contributors

---

⭐ **Star this repo if you find it helpful!**

**Full observability stack with metrics, logs, and distributed tracing ready for production!** 🚀
