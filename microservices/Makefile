# ==========================
# List of services
# ==========================
SERVICES=auth-service gateway-service product-service order-service file-service

# ==========================
# Default target
# ==========================
.PHONY: all
all: build docker-compose-up

# ==========================
# 1️⃣ Build JARs for all services using their own Makefiles
# ==========================
.PHONY: build
build:
	@echo "🔨 Building all Spring Boot services..."
	@for service in $(SERVICES); do \
		echo "➡ Building $$service..."; \
		cd $$service && make build && cd ..; \
	done

# ==========================
# 2️⃣ Build Docker images for all services
# ==========================
.PHONY: docker-build
docker-build:
	@echo "🐳 Building Docker images..."
	@for service in $(SERVICES); do \
		echo "➡ Building image for $$service..."; \
		docker build -t ibrahimlboss/$$service:latest $$service; \
	done

# ==========================
# 3️⃣ Start Docker Compose stack
# ==========================
.PHONY: docker-compose-up
docker-compose-up:
	@echo "🚀 Starting Docker Compose..."
	docker compose up --build -d

# ==========================
# 4️⃣ Stop Docker Compose stack
# ==========================
.PHONY: docker-compose-down
docker-compose-down:
	@echo "🛑 Stopping Docker Compose..."
	docker compose down

# ==========================
# 5️⃣ Push Docker images to Docker Hub
# ==========================
.PHONY: docker-push
docker-push:
	@echo "📤 Pushing Docker images..."
	@for service in $(SERVICES); do \
		echo "➡ Pushing image for $$service..."; \
		docker push ibrahimlboss/$$service:latest; \
	done
