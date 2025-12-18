# Microservices Kubernetes Deployment

This repository contains a microservices application deployed on Kubernetes using Minikube. It includes services for authentication, products, orders, gateway routing, PostgreSQL, RabbitMQ, and now **logging (ELK)** and **monitoring (Prometheus/Grafana)**.

---

## Prerequisites

- [Docker](https://www.docker.com/get-started) installed
- [Minikube](https://minikube.sigs.k8s.io/docs/start/) installed
- [kubectl](https://kubernetes.io/docs/tasks/tools/) installed
- Internet access to pull Docker images

---

## Steps to Start the Microservices

### 1. Start Minikube

Start Minikube with enough resources:

```bash
minikube start --cpus=4 --memory=6000
```

Check the cluster:

```bash
kubectl get nodes
```

---

### 2. Enable Minikube Addons

Enable the Ingress controller:

```bash
minikube addons enable ingress
```

---

### 3. Create Configs

Create namespace:

```bash
kubectl apply -f .\k8s\config\namespace.yaml
```

Create Kubernetes secrets for sensitive data:

```bash
kubectl apply -f .\k8s\config\secrets.yaml -n microservices
```

Create Kubernetes configmap for sensitive data:

```bash
kubectl apply -f .\k8s\config\configmap.yaml -n microservices
```

---

### 4. Deploy Services and Deployments

Deploy PostgreSQL and RabbitMQ:

```bash
kubectl apply -f .\k8s\services\postgres-configmap.yaml -n microservices
kubectl apply -f .\k8s\services\postgres-deploy.yaml -n microservices
kubectl apply -f .\k8s\services\postgres-service.yaml -n microservices
kubectl apply -f .\k8s\rabbitmq\values.yaml -n microservices
```

Deploy microservices (deployments + services):

```bash
# Auth Service
kubectl apply -f .\k8s\services\auth-service-deploy.yaml -n microservices
kubectl apply -f .\k8s\services\auth-service-service.yaml -n microservices

# Product Service
kubectl apply -f .\k8s\services\product-service-deploy.yaml -n microservices
kubectl apply -f .\k8s\services\product-service-service.yaml -n microservices

# Order Service
kubectl apply -f .\k8s\services\order-service-deploy.yaml -n microservices
kubectl apply -f .\k8s\services\order-service-service.yaml -n microservices

# Payment Service
kubectl apply -f .\k8s\services\payment-service-deploy.yaml -n microservices
kubectl apply -f .\k8s\services\payment-service-service.yaml -n microservices

# Gateway Service
kubectl apply -f .\k8s\services\gateway-service-deploy.yaml -n microservices
kubectl apply -f .\k8s\services\gateway-service-service.yaml -n microservices

# File Service (if needed)
kubectl apply -f .\k8s\services\file-service-deploy.yaml -n microservices
kubectl apply -f .\k8s\services\file-service-service.yaml -n microservices
```

Or deploy all at once:

```bash
kubectl apply -f .\k8s\services\ -n microservices
```

Check pods and services:

```bash
kubectl get pods -n microservices -w
kubectl get svc -n microservices
```

---

### 5. Deploy Ingress

Apply the ingress configuration:

```bash
kubectl apply -f .\k8s\gateway\ingress.yaml -n microservices
```

---

### 6. Deploy Logging Stack (ELK)

Deploy Elasticsearch, Logstash, and Kibana for centralized logging:

```bash
kubectl apply -f .\k8s\logging\elasticsearch-statefulset.yaml -n microservices
kubectl apply -f .\k8s\logging\elasticsearch-service.yaml -n microservices
kubectl apply -f .\k8s\logging\logstash-configmap.yaml -n microservices
kubectl apply -f .\k8s\logging\logstash-deployment.yaml -n microservices
kubectl apply -f .\k8s\logging\logstash-service.yaml -n microservices
kubectl apply -f .\k8s\logging\kibana-deployment.yaml -n microservices
kubectl apply -f .\k8s\logging\kibana-service.yaml -n microservices
```

Verify deployment:

```bash
kubectl get all -n microservices | findstr "elasticsearch logstash kibana"
```

Access Kibana:

```bash
minikube service kibana -n microservices
# Or access at http://<node-ip>:30002
```

---

### 7. Deploy Monitoring Stack (Prometheus & Grafana)

Deploy Prometheus and Grafana for metrics monitoring:

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

Verify deployment:

```bash
kubectl get all -n microservices | findstr "prometheus grafana"
```

Access Grafana:

```bash
minikube service grafana -n microservices
# Or access at http://<node-ip>:30001
# Default credentials: admin / admin
```

Access Prometheus (via port-forward):

```bash
kubectl port-forward -n microservices svc/prometheus 9090:9090
# Access at http://localhost:9090
```

> **Note**: For detailed explanation of what each component does, see [docs/LOGGING_MONITORING_EXPLAINED.md](docs/LOGGING_MONITORING_EXPLAINED.md)

---

### 8. Access Services

#### List All Services

Check all available services:

```bash
kubectl get svc -n microservices
```

#### Access Monitoring & Logging Dashboards

**Kibana (Logging Dashboard)** - NodePort 30002:

```bash
minikube service kibana -n microservices
# Access at http://127.0.0.1:<tunnel-port>
# Keep terminal open while using
```

**Grafana (Monitoring Dashboard)** - NodePort 30001:

```bash
minikube service grafana -n microservices
# Access at http://127.0.0.1:<tunnel-port>
# Default credentials: admin / admin
# Keep terminal open while using
```

**Prometheus (Metrics)** - ClusterIP (requires port-forward):

```bash
kubectl port-forward -n microservices svc/prometheus 9090:9090
# Access at http://localhost:9090
```

#### Access Microservices via Ingress

The microservices are exposed through the ingress controller. Get the ingress IP:

```bash
kubectl get ingress -n microservices
minikube ip
```

Then access services at:

```
http://<minikube-ip>/api/v1/auth/*
http://<minikube-ip>/api/v1/products/*
http://<minikube-ip>/api/v1/orders/*
http://<minikube-ip>/api/v1/payments/*
```

#### Useful Debugging Commands

```bash
# Check all resources
kubectl get all -n microservices

# Check service endpoints
kubectl get endpoints -n microservices

# Check pod status
kubectl get pods -n microservices -w

# View pod logs
kubectl logs <pod-name> -n microservices

# Follow logs in real-time
kubectl logs -f <pod-name> -n microservices
```

---

### 9. Access Services

- Use the gateway service URL provided by `minikube tunnel` or ingress.
- Example:

```bash
curl http://localhost:58693/api/v1/auth/signup
```

> Note: Ports may vary depending on the minikube tunnel assignment.

---

### 10. Restarting Services

To restart a service after updating its image or config:

```bash
kubectl rollout restart deployment <deployment-name> -n microservices
```

Check rollout status:

```bash
kubectl get pods -n microservices -w
```

---

### 11. Logs & Debugging

Get logs of a specific pod:

```bash
kubectl logs <pod-name> -n microservices
```

Follow logs of all pods in real-time:

```bash
kubectl logs -n microservices -l app=<service-label> --follow
```

---

### Notes

- Make sure all services point to the correct ports (gateway to microservices on port 8080).
- Ensure secrets and environment variables are properly configured.
- JWT secret must be 256-bit for HMAC-SHA algorithms.
- During rolling updates, old pods terminate automatically while new pods come up.
- **All services run in the same namespace**: `microservices`

---
