# Global Configuration

This directory contains infrastructure configurations for monitoring and logging that are environment-agnostic and can be deployed on both Docker Compose and Kubernetes.

## Directory Structure

```
globalconf/
├── monitoring/
│   ├── docker/              # Docker Compose configurations
│   │   ├── prometheus.yml
│   │   └── grafana/
│   │       └── provisioning/
│   └── k8s/                 # Kubernetes manifests
│       ├── prometheus.yaml
│       └── grafana.yaml
└── logging/
    ├── docker/              # Docker Compose configurations
    │   └── logstash/
    │       └── pipeline/
    │           └── logstash.conf
    └── k8s/                 # Kubernetes manifests
        ├── elasticsearch.yaml
        ├── logstash.yaml
        └── kibana.yaml
```

## Docker Compose Deployment

The monitoring and logging stacks are automatically deployed when you run:

```bash
cd microservices
docker-compose up -d
```

### Access Points
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001 (admin/admin)
- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601

## Kubernetes Deployment

### Prerequisites
- Kubernetes cluster (minikube, kind, or cloud provider)
- kubectl configured
- Sufficient cluster resources

### Deploy Monitoring Stack

```bash
# Deploy Prometheus
kubectl apply -f globalconf/monitoring/k8s/prometheus.yaml

# Deploy Grafana
kubectl apply -f globalconf/monitoring/k8s/grafana.yaml

# Verify deployments
kubectl get all -n monitoring
```

**Access Grafana:**
```bash
# Get the NodePort
kubectl get svc grafana -n monitoring

# Access via: http://<node-ip>:30001
# Default credentials: admin/admin
```

### Deploy Logging Stack

```bash
# Deploy Elasticsearch
kubectl apply -f globalconf/logging/k8s/elasticsearch.yaml

# Wait for Elasticsearch to be ready
kubectl wait --for=condition=ready pod -l app=elasticsearch -n logging --timeout=300s

# Deploy Logstash
kubectl apply -f globalconf/logging/k8s/logstash.yaml

# Deploy Kibana
kubectl apply -f globalconf/logging/k8s/kibana.yaml

# Verify deployments
kubectl get all -n logging
```

**Access Kibana:**
```bash
# Get the NodePort
kubectl get svc kibana -n logging

# Access via: http://<node-ip>:30002
```

### Update Microservices for Kubernetes

Update your microservice logback configurations to point to the Logstash service:

```xml
<destination>logstash.logging.svc.cluster.local:5001</destination>
```

## Configuration Details

### Monitoring Stack

**Prometheus:**
- Scrapes metrics from all microservices via `/actuator/prometheus`
- Stores metrics for 15 days (configurable)
- Resource limits: 1Gi memory, 500m CPU

**Grafana:**
- Pre-configured Prometheus datasource
- Persistent storage for dashboards
- Resource limits: 512Mi memory, 200m CPU

### Logging Stack

**Elasticsearch:**
- Single-node cluster (for development)
- 20Gi persistent storage
- Resource limits: 2Gi memory, 1000m CPU

**Logstash:**
- Receives logs via TCP on port 5001
- Parses JSON logs from microservices
- Indexes to Elasticsearch

**Kibana:**
- Web UI for log exploration
- Resource limits: 1Gi memory, 500m CPU

## Scaling for Production

### For Production Kubernetes Deployment:

1. **Elasticsearch**: Use StatefulSet with 3+ replicas
2. **Add Ingress**: Replace NodePort with Ingress for external access
3. **Resource Limits**: Adjust based on load
4. **Persistent Storage**: Use appropriate StorageClass for your cloud provider
5. **Security**: Enable authentication and TLS
6. **Backup**: Configure snapshot repository

### Example Production Changes:

```yaml
# Use Ingress instead of NodePort
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: grafana
  namespace: monitoring
spec:
  rules:
  - host: grafana.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: grafana
            port:
              number: 3000
```

## Troubleshooting

### Docker Compose Issues

**Containers not starting:**
```bash
docker-compose logs prometheus
docker-compose logs grafana
docker-compose logs elasticsearch
```

**Volume permission issues:**
```bash
docker-compose down -v  # Remove volumes
docker-compose up -d    # Recreate
```

### Kubernetes Issues

**Pods not starting:**
```bash
kubectl describe pod <pod-name> -n monitoring
kubectl logs <pod-name> -n monitoring
```

**Storage issues:**
```bash
kubectl get pvc -n monitoring
kubectl get pv
```

**Service connectivity:**
```bash
kubectl exec -it <pod-name> -n monitoring -- curl http://prometheus:9090
```

## Maintenance

### Backup Grafana Dashboards
```bash
# Docker Compose
docker cp grafana:/var/lib/grafana ./grafana-backup

# Kubernetes
kubectl cp monitoring/grafana-<pod-id>:/var/lib/grafana ./grafana-backup
```

### Clean Up

**Docker Compose:**
```bash
cd microservices
docker-compose down
docker volume prune  # Remove unused volumes
```

**Kubernetes:**
```bash
kubectl delete namespace monitoring
kubectl delete namespace logging
```
