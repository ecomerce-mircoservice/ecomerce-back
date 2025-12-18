# Logging & Monitoring Components Explained

## What is Helm vs kubectl?

### kubectl (What We're Using) ✅

- **Simple**: Apply YAML files directly
- **Command**: `kubectl apply -f file.yaml`
- **What you see is what you get** - no magic, no templates
- **Perfect for learning** and small-to-medium projects

### Helm (Removed - Too Complex) ❌

- **Complex**: Uses templates with variables
- **Like a package manager** for Kubernetes (think npm for Node.js)
- **Overkill** for your current setup
- **We removed it** to keep things simple

---

## Architecture Overview

```
Your Microservices (microservices namespace)
├── auth-service, product-service, order-service, etc.
├── PostgreSQL, RabbitMQ
├── Gateway
│
├── LOGGING STACK (ELK) ← NEW
│   ├── Elasticsearch (stores logs)
│   ├── Logstash (processes logs)
│   └── Kibana (visualize logs)
│
└── MONITORING STACK ← NEW
    ├── Prometheus (collects metrics)
    └── Grafana (visualize metrics)
```

**Everything runs in ONE namespace: `microservices`**

---

## Logging Stack (ELK) - What Each Part Does

### 1. Elasticsearch

**What it is**: A database for storing logs  
**Why you need it**: Stores all your application logs in a searchable format

**Files**:

- `elasticsearch-statefulset.yaml` - The main Elasticsearch deployment
  - **StatefulSet** (not Deployment) because it needs persistent storage
  - **20Gi storage** - Stores your logs
  - **Ports**: 9200 (HTTP API), 9300 (cluster communication)
- `elasticsearch-service.yaml` - How other services connect to Elasticsearch
  - **ClusterIP** - Only accessible inside Kubernetes (secure)

**Key Settings**:

```yaml
env:
  - discovery.type: "single-node" # Running alone, not in a cluster
  - xpack.security.enabled: "false" # No password (for development)
  - ES_JAVA_OPTS: "-Xms512m -Xmx512m" # Memory: min 512MB, max 512MB
```

---

### 2. Logstash

**What it is**: A log processor  
**Why you need it**: Takes logs from your services, processes them, sends to Elasticsearch

**Files**:

- `logstash-configmap.yaml` - Configuration for how Logstash processes logs

  - **Input**: Listens on port 5001 for logs (TCP)
  - **Filter**: Parses JSON logs, extracts timestamp
  - **Output**: Sends to Elasticsearch with daily indices

- `logstash-deployment.yaml` - The Logstash application
  - Mounts the ConfigMap as configuration
- `logstash-service.yaml` - Exposes port 5001
  - **Your Spring Boot services send logs here**

**How it works**:

```
Your Service → Logstash (port 5001) → Processes → Elasticsearch
```

**Pipeline Configuration** (in ConfigMap):

```
Input: TCP port 5001 (receives logs from your services)
  ↓
Filter: Parse JSON, extract timestamp
  ↓
Output: Elasticsearch (creates daily indices: microservices-logs-2024.12.17)
```

---

### 3. Kibana

**What it is**: A web UI for viewing logs  
**Why you need it**: Search and visualize your logs stored in Elasticsearch

**Files**:

- `kibana-deployment.yaml` - The Kibana application
  - Connects to Elasticsearch at `http://elasticsearch:9200`
- `kibana-service.yaml` - Web access
  - **NodePort 30002** - Access from your browser at `http://localhost:30002`

**What you can do**:

- Search logs by service, time, error level
- Create dashboards
- Set up alerts

---

## Monitoring Stack - What Each Part Does

### 1. Prometheus

**What it is**: A metrics collector  
**Why you need it**: Collects performance metrics from your services (CPU, memory, request counts, etc.)

**Files**:

- `prometheus-configmap.yaml` - Configuration for what to monitor

  - **Scrape configs**: Tells Prometheus where to collect metrics
  - Monitors: auth-service, product-service, order-service, payment-service, gateway-service
  - **Endpoint**: `/actuator/prometheus` (Spring Boot Actuator)

- `prometheus-pvc.yaml` - Persistent storage (10Gi)

  - Stores metrics history for 15 days

- `prometheus-deployment.yaml` - The Prometheus application

  - Scrapes metrics every 15 seconds
  - Retention: 15 days

- `prometheus-service.yaml` - Internal access
  - **ClusterIP** - Only accessible inside Kubernetes
  - **Port**: 9090

**How it works**:

```
Prometheus → Scrapes /actuator/prometheus every 15s → Stores metrics
```

**What it monitors**:

- HTTP request counts
- Response times
- JVM memory usage
- CPU usage
- Custom metrics you define

---

### 2. Grafana

**What it is**: A visualization dashboard  
**Why you need it**: Creates beautiful charts and graphs from Prometheus metrics

**Files**:

- `grafana-configmap.yaml` - Auto-configures Prometheus as data source

  - Grafana automatically knows where Prometheus is

- `grafana-pvc.yaml` - Persistent storage (5Gi)

  - Stores your dashboards and settings

- `grafana-deployment.yaml` - The Grafana application

  - Default credentials: `admin` / `admin`

- `grafana-service.yaml` - Web access
  - **NodePort 30001** - Access from browser at `http://localhost:30001`

**What you can do**:

- Create dashboards with charts
- Monitor service health in real-time
- Set up alerts (email when CPU > 80%)
- Import pre-built dashboards

---

## Kubernetes Resource Types Explained

### StatefulSet (Elasticsearch)

- For **stateful** applications (need persistent storage)
- Each pod gets its own persistent volume
- Pod names are predictable: `elasticsearch-0`, `elasticsearch-1`

### Deployment (Everything else)

- For **stateless** applications
- Pods can be replaced easily
- Used for: Logstash, Kibana, Prometheus, Grafana

### Service

- **How pods communicate** with each other
- Types:
  - **ClusterIP**: Internal only (Elasticsearch, Logstash, Prometheus)
  - **NodePort**: External access (Kibana:30002, Grafana:30001)

### ConfigMap

- Stores **configuration** as key-value pairs
- Used for: Logstash pipeline, Prometheus scrape config, Grafana datasources

### PersistentVolumeClaim (PVC)

- Requests **storage** from Kubernetes
- Used for: Prometheus (10Gi), Grafana (5Gi), Elasticsearch (20Gi)

---

## How to Deploy

### Step 1: Deploy Logging Stack

```bash
kubectl apply -f .\k8s\logging\elasticsearch-statefulset.yaml -n microservices
kubectl apply -f .\k8s\logging\elasticsearch-service.yaml -n microservices
kubectl apply -f .\k8s\logging\logstash-configmap.yaml -n microservices
kubectl apply -f .\k8s\logging\logstash-deployment.yaml -n microservices
kubectl apply -f .\k8s\logging\logstash-service.yaml -n microservices
kubectl apply -f .\k8s\logging\kibana-deployment.yaml -n microservices
kubectl apply -f .\k8s\logging\kibana-service.yaml -n microservices
```

### Step 2: Deploy Monitoring Stack

```bash
kubectl apply -f .\k8s\monitoring\prometheus-configmap.yaml -n microservices
kubectl apply -f .\k8s\monitoring\prometheus-pvc.yaml -n microservices
kubectl apply -f .\k8s\monitoring\prometheus-deployment.yaml -n microservices
kubectl apply -f .\k8s\monitoring\prometheus-service.yaml -n microservices
kubectl apply -f .\k8s\monitoring\grafana-configmap.yaml -n microservices
kubectl apply -f .\k8s\monitoring\grafana-pvc.yaml -n microservices
kubectl apply -f .\k8s\monitoring\grafana-deployment.yaml -n microservices
kubectl apply -f .\k8s\monitoring\grafana-service.yaml -n microservices
```

### Step 3: Verify

```bash
kubectl get all -n microservices
```

### Step 4: Access UIs

- **Kibana**: `minikube service kibana -n microservices` or `http://localhost:30002`
- **Grafana**: `minikube service grafana -n microservices` or `http://localhost:30001`
  - Login: `admin` / `admin`

---

## What Changed from Docker Compose

### Before (Docker Compose)

```yaml
services:
  elasticsearch:
    image: elasticsearch:8.11.0
    ports: ["9200:9200"]

  logstash:
    image: logstash:8.11.0
    ports: ["5001:5001"]

  kibana:
    image: kibana:8.11.0
    ports: ["5601:5601"]
```

### After (Kubernetes)

- **Separate files** for each component
- **Deployments** instead of containers
- **Services** for networking
- **ConfigMaps** for configuration
- **PVCs** for persistent storage
- **Health checks** for reliability

---

## Next Steps

1. **Configure your Spring Boot services** to send logs to Logstash:

   ```yaml
   # application.yml
   logging:
     config: classpath:logback-spring.xml
   ```

   Add Logstash appender to send logs to `logstash:5001`

2. **Enable Actuator** in Spring Boot for Prometheus:

   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   <dependency>
       <groupId>io.micrometer</groupId>
       <artifactId>micrometer-registry-prometheus</artifactId>
   </dependency>
   ```

3. **Create Grafana dashboards** for your services

4. **Set up alerts** in Prometheus/Grafana

---

## Troubleshooting

### Elasticsearch won't start

- Check if it has enough memory: `kubectl describe pod elasticsearch-0 -n microservices`
- Elasticsearch needs at least 1Gi of memory

### Can't access Kibana/Grafana

- Check if services are running: `kubectl get svc -n microservices`
- For Minikube: Use `minikube service <service-name> -n microservices`

### Prometheus not scraping metrics

- Ensure your services have `/actuator/prometheus` endpoint enabled
- Check Prometheus targets: Port-forward and visit `http://localhost:9090/targets`

### Logs not appearing in Kibana

- Check if Logstash is running: `kubectl get pods -n microservices`
- Verify your services are sending logs to `logstash:5001`
- Check Logstash logs: `kubectl logs <logstash-pod> -n microservices`
