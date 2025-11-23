# Variables
APP_NAME=product-service
VERSION=0.0.1-SNAPSHOT
REGISTRY_URL=ibrahimlboss
IMAGE_NAME=$(REGISTRY_URL)/$(APP_NAME)
JAR_FILE=target/$(APP_NAME)-$(VERSION).jar

# Default target
all: run-compose build jar docker run

# 1️⃣ Build the project (Maven)
build:
	@echo "🔨 Building the Java project..."
	JAVA_HOME="/c/Program Files/Java/jdk-21" ./mvnw clean package -DskipTests

# 2️⃣ Build JAR (already done by Maven build)
jar:
	@echo "📦 JAR file generated at $(JAR_FILE)"
	@ls -lh $(JAR_FILE)

# 3️⃣ Build Docker image
docker:
	@echo "🐳 Building Docker image..."
	docker build -t $(IMAGE_NAME):latest .

# Run Docker container
run:
	@echo "🚀 Running Docker container..."
	docker run -d --name $(APP_NAME) -p 8081:8081 $(IMAGE_NAME):latest	

# 4️⃣ Push Docker image (optional)
push:
	@echo "🚀 Pushing Docker image..."
	docker push $(IMAGE_NAME):latest

# 5️⃣ Clean
clean:
	@echo "🧹 Cleaning..."
	./mvnw clean
	docker rmi $(IMAGE_NAME):latest || true

# Stop Docker container
stop:
	@echo "🛑 Stopping Docker container..."
	docker stop $(APP_NAME) || true
# Run the docker-compose
run-compose:
	@echo "🚀 Running docker-compose..."
	docker-compose up -d	


