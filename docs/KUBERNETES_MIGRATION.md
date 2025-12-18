# Kubernetes Migration Guide: Logging & Monitoring Services

This guide provides comprehensive instructions for migrating the logging (ELK Stack) and monitoring (Prometheus/Grafana) services from Docker Compose to Kubernetes.

## Table of Contents

- [Overview](#overview)
- [Architecture Comparison](#architecture-comparison)
- [Prerequisites](#prerequisites)
- [Deployment Methods](#deployment-methods)
  - [Method 1: Direct kubectl Apply](#method-1-direct-kubectl-apply)
  - [Method 2: Helm Charts](#method-2-helm-charts)
- [Accessing Services](#accessing-services)
- [Configuration Management](#configuration-management)
- [Troubleshooting](#troubleshooting)
- [Rollback Procedures](#rollback-procedures)

## Overview

This migration moves the logging and monitoring infrastructure from Docker Compose to Kubernetes, providing:

- **Better scalability**: Kubernetes can automatically scale services based on demand
- **High availability**: Built-in health checks and automatic restarts
- **Resource management**: Fine-grained control over CPU and memory allocation
- **Configuration flexibility**: Helm charts for easy customization
- **Production readiness**: Industry-standard orchestration platform

### What's Changed

- **Logging Stack (ELK)**:

  - Elasticsearch: Deployed as StatefulSet with persistent storage
  - Logstash: Deployed as Deployment with ConfigMap for pipeline configuration
  - Kibana: Deployed as Deployment with NodePort access

- **Monitoring Stack**:

  - Prometheus: Deployed as Deployment with ConfigMap and persistent storage
  - Grafana: Deployed as Deployment with datasource provisioning

- **Removed**: Loki service (not being used)

## Architecture Comparison

### Docker Compose (Before)

```
docker-compose.yaml
├── elasticsearch (container)
├── logstash (container)
├── kibana (container)
├── prometheus (container)
└── grafana (container)
```

### Kubernetes (After)

```
k8s/
├── logging/
│   ├── namespace.yaml
│   ├── elasticsearch-statefulset.yaml
│   ├── elasticsearch-service.yaml
│   ├── logstash-configmap.yaml
│   ├── logstash-deployment.yaml
│   ├── logstash-service.yaml
│   ├── kibana-deployment.yaml
│   ├── kibana-service.yaml
│   └── helm/
│       ├── Chart.yaml
│       ├── values.yaml
│       └── templates/
└── monitoring/
    ├── namespace.yaml
    ├── prometheus-configmap.yaml
    ├── prometheus-pvc.yaml
    ├── prometheus-deployment.yaml
    ├── prometheus-service.yaml
    ├── grafana-configmap.yaml
    ├── grafana-pvc.yaml
    ├── grafana-deployment.yaml
    ├── grafana-service.yaml
    └── helm/
        ├── Chart.yaml
        ├── values.yaml
        └── templates/
```

## Prerequisites

Before starting the migration, ensure you have:

1. **Kubernetes Cluster**: A running Kubernetes cluster (Minikube, Kind, or cloud provider)
2. **kubectl**: Installed and configured to access your cluster
3. **Helm** (optional): Version 3.x for Helm-based deployment
4. **Storage Class**: Default storage class configured for persistent volumes

Verify your setup:

```bash
# Check kubectl connection
kubectl cluster-info

# Check available storage classes
kubectl get storageclass

# Check Helm installation (if using Helm)
helm version
```

## Deployment Methods

You can deploy the logging and monitoring stacks using either direct kubectl commands or Helm charts.

### Method 1: Direct kubectl Apply

This method applies the Kubernetes manifests directly without Helm.

#### Deploy Logging Stack

```bash
# Navigate to the k8s directory
cd k8s/logging

# Apply all logging resources
kubectl apply -f namespace.yaml
kubectl apply -f elasticsearch-statefulset.yaml
kubectl apply -f elasticsearch-service.yaml
kubectl apply -f logstash-configmap.yaml
kubectl apply -f logstash-deployment.yaml
kubectl apply -f logstash-service.yaml
kubectl apply -f kibana-deployment.yaml
kubectl apply -f kibana-service.yaml

# Verify deployment
kubectl get all -n logging
```

#### Deploy Monitoring Stack

```bash
# Navigate to the monitoring directory
cd ../monitoring

# Apply all monitoring resources
kubectl apply -f namespace.yaml
kubectl apply -f prometheus-configmap.yaml
kubectl apply -f prometheus-pvc.yaml
kubectl apply -f prometheus-deployment.yaml
kubectl apply -f prometheus-service.yaml
kubectl apply -f grafana-configmap.yaml
kubectl apply -f grafana-pvc.yaml
kubectl apply -f grafana-deployment.yaml
kubectl apply -f grafana-service.yaml

# Verify deployment
kubectl get all -n monitoring
```

### Method 2: Helm Charts

This method uses Helm charts for easier configuration management and upgrades.

#### Deploy Logging Stack with Helm

```bash
# Navigate to the logging helm directory
cd k8s/logging/helm

# Install with default values
helm install logging-stack .

# Or install with custom values
helm install logging-stack . -f custom-values.yaml

# Or override specific values
helm install logging-stack . \
  --set elasticsearch.persistence.size=50Gi \
  --set kibana.service.nodePort=30003

# Verify deployment
helm list
kubectl get all -n logging
```

#### Deploy Monitoring Stack with Helm

```bash
# Navigate to the monitoring helm directory
cd ../../monitoring/helm

# Install with default values
helm install monitoring-stack .

# Or install with custom values
helm install monitoring-stack . -f custom-values.yaml

# Or override specific values
helm install monitoring-stack . \
  --set prometheus.persistence.size=50Gi \
  --set grafana.admin.password=securepassword

# Verify deployment
helm list
kubectl get all -n monitoring
```

## Accessing Services

### Kibana

Kibana is exposed via NodePort on port 30002.

```bash
# Get the NodePort URL
kubectl get svc -n logging kibana

# Access Kibana
# For Minikube:
minikube service kibana -n logging

# For other clusters:
# http://<node-ip>:30002
```

### Grafana

Grafana is exposed via NodePort on port 30001.

```bash
# Get the NodePort URL
kubectl get svc -n monitoring grafana

# Access Grafana
# For Minikube:
minikube service grafana -n monitoring

# For other clusters:
# http://<node-ip>:30001

# Default credentials:
# Username: admin
# Password: admin (change this in production!)
```

### Prometheus

Prometheus is exposed as ClusterIP (internal only). To access it:

```bash
# Port-forward to access locally
kubectl port-forward -n monitoring svc/prometheus 9090:9090

# Access at http://localhost:9090
```

### Elasticsearch

Elasticsearch is exposed as ClusterIP (internal only). To access it:

```bash
# Port-forward to access locally
kubectl port-forward -n logging svc/elasticsearch 9200:9200

# Access at http://localhost:9200
```

## Configuration Management

### Customizing Logging Stack

Edit the Helm values file at `k8s/logging/helm/values.yaml`:

```yaml
# Example: Increase Elasticsearch storage
elasticsearch:
  persistence:
    size: 50Gi

# Example: Change Kibana service type
kibana:
  service:
    type: LoadBalancer # Instead of NodePort
```

Apply changes:

```bash
helm upgrade logging-stack ./helm -f values.yaml
```

### Customizing Monitoring Stack

Edit the Helm values file at `k8s/monitoring/helm/values.yaml`:

```yaml
# Example: Add custom Prometheus scrape config
prometheus:
  scrapeConfigs:
    jobs:
      - name: custom-service
        metricsPath: /metrics
        targets:
          - custom-service.default.svc.cluster.local:8080

# Example: Change Grafana admin password
grafana:
  admin:
    password: my-secure-password
```

Apply changes:

```bash
helm upgrade monitoring-stack ./helm -f values.yaml
```

### Updating Logstash Pipeline

To modify the Logstash pipeline configuration:

1. Edit `k8s/logging/logstash-configmap.yaml`
2. Apply the changes:

```bash
kubectl apply -f logstash-configmap.yaml
kubectl rollout restart deployment/logstash -n logging
```

### Updating Prometheus Scrape Targets

To add or modify Prometheus scrape targets:

1. Edit `k8s/monitoring/prometheus-configmap.yaml`
2. Apply the changes:

```bash
kubectl apply -f prometheus-configmap.yaml
kubectl rollout restart deployment/prometheus -n monitoring
```

## Troubleshooting

### Pods Not Starting

Check pod status and logs:

```bash
# Check pod status
kubectl get pods -n logging
kubectl get pods -n monitoring

# Describe pod for events
kubectl describe pod <pod-name> -n <namespace>

# View pod logs
kubectl logs <pod-name> -n <namespace>
```

### Persistent Volume Issues

Check PVC and PV status:

```bash
# Check PVCs
kubectl get pvc -n logging
kubectl get pvc -n monitoring

# Check PVs
kubectl get pv

# Describe PVC for details
kubectl describe pvc <pvc-name> -n <namespace>
```

**Common fixes**:

- Ensure a default storage class is configured
- Check if the storage class supports the requested access mode
- Verify sufficient storage is available

### Elasticsearch Not Ready

Elasticsearch may take a few minutes to start. Check:

```bash
# Check Elasticsearch logs
kubectl logs -n logging elasticsearch-0

# Check Elasticsearch health
kubectl exec -n logging elasticsearch-0 -- curl -X GET "localhost:9200/_cluster/health?pretty"
```

**Common issues**:

- Insufficient memory (increase resource limits)
- Persistent volume not mounting
- Java heap size too large for available memory

### Kibana Connection Issues

If Kibana can't connect to Elasticsearch:

```bash
# Check Kibana logs
kubectl logs -n logging deployment/kibana

# Verify Elasticsearch service
kubectl get svc -n logging elasticsearch

# Test connectivity from Kibana pod
kubectl exec -n logging deployment/kibana -- curl http://elasticsearch:9200
```

### Prometheus Not Scraping Targets

Check Prometheus configuration and targets:

```bash
# Port-forward to Prometheus
kubectl port-forward -n monitoring svc/prometheus 9090:9090

# Visit http://localhost:9090/targets to see target status
```

**Common issues**:

- Service names incorrect in scrape config
- Services not in the expected namespace
- Actuator endpoints not enabled in Spring Boot services

### Grafana Datasource Issues

If Grafana can't connect to Prometheus:

```bash
# Check Grafana logs
kubectl logs -n monitoring deployment/grafana

# Verify Prometheus service
kubectl get svc -n monitoring prometheus

# Test connectivity from Grafana pod
kubectl exec -n monitoring deployment/grafana -- curl http://prometheus:9090
```

## Rollback Procedures

### Rollback to Docker Compose

If you need to rollback to Docker Compose:

1. **Stop Kubernetes deployments**:

```bash
# Delete logging stack
kubectl delete namespace logging

# Delete monitoring stack
kubectl delete namespace monitoring

# Or if using Helm:
helm uninstall logging-stack
helm uninstall monitoring-stack
```

2. **Start Docker Compose services**:

```bash
cd microservices
docker-compose up -d elasticsearch logstash kibana prometheus grafana
```

### Rollback Helm Release

To rollback a Helm release to a previous version:

```bash
# List release history
helm history logging-stack
helm history monitoring-stack

# Rollback to previous version
helm rollback logging-stack
helm rollback monitoring-stack

# Rollback to specific revision
helm rollback logging-stack 2
```

### Rollback kubectl Changes

To rollback a deployment:

```bash
# View rollout history
kubectl rollout history deployment/kibana -n logging

# Rollback to previous version
kubectl rollout undo deployment/kibana -n logging

# Rollback to specific revision
kubectl rollout undo deployment/kibana -n logging --to-revision=2
```

## Best Practices

1. **Resource Limits**: Always set resource requests and limits to prevent resource exhaustion
2. **Persistent Storage**: Use persistent volumes for stateful services (Elasticsearch, Prometheus, Grafana)
3. **Health Checks**: Configure readiness and liveness probes for all deployments
4. **Secrets Management**: Use Kubernetes Secrets for sensitive data (passwords, API keys)
5. **Monitoring**: Monitor the monitoring stack itself to ensure observability
6. **Backup**: Regularly backup persistent volume data
7. **Security**: Change default passwords in production environments
8. **Namespaces**: Use separate namespaces for isolation and resource management

## Next Steps

After successful migration:

1. **Configure Spring Boot services** to send logs to Logstash (port 5001)
2. **Enable Actuator endpoints** in all microservices for Prometheus scraping
3. **Create Grafana dashboards** for visualizing metrics
4. **Set up alerts** in Prometheus for critical metrics
5. **Configure log retention** policies in Elasticsearch
6. **Implement backup strategy** for persistent data
7. **Review and adjust resource limits** based on actual usage

## Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Documentation](https://helm.sh/docs/)
- [Elasticsearch on Kubernetes](https://www.elastic.co/guide/en/cloud-on-k8s/current/index.html)
- [Prometheus Operator](https://github.com/prometheus-operator/prometheus-operator)
- [Grafana Documentation](https://grafana.com/docs/)
