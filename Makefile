# ==========================
# List of services
# ==========================
SERVICES=auth-service gateway-service product-service order-service payment-service file-service

# ==========================
# Default target
# ==========================
.PHONY: all
all: build-images 

# ==========================
# 1️⃣ Build JARs, Docker images and push them to Docker Hub for all services using th	eir own Makefiles
# ==========================
.PHONY: build-images
build-images:
	@echo "🔨 Building all Spring Boot services..."
	@for service in $(SERVICES); do \
		echo "➡ Building $$service..."; \
		cd microservices/$$service && make build && make docker && make push && cd ../..; \
	done