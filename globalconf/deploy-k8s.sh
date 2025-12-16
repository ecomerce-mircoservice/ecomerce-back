#!/bin/bash

# Kubernetes Deployment Script for Monitoring and Logging

set -e

echo "🚀 Deploying Monitoring and Logging Infrastructure to Kubernetes"

# Function to wait for deployment
wait_for_deployment() {
    namespace=$1
    deployment=$2
    echo "⏳ Waiting for $deployment in namespace $namespace..."
    kubectl wait --for=condition=available --timeout=300s deployment/$deployment -n $namespace
}

# Function to wait for statefulset
wait_for_statefulset() {
    namespace=$1
    statefulset=$2
    echo "⏳ Waiting for $statefulset in namespace $namespace..."
    kubectl wait --for=condition=ready --timeout=300s pod -l app=$statefulset -n $namespace
}

# Deploy Monitoring Stack
echo ""
echo "📊 Deploying Monitoring Stack (Prometheus + Grafana)..."
kubectl apply -f globalconf/monitoring/k8s/prometheus.yaml
wait_for_deployment monitoring prometheus

kubectl apply -f globalconf/monitoring/k8s/grafana.yaml
wait_for_deployment monitoring grafana

echo "✅ Monitoring stack deployed successfully!"
echo "   Grafana: http://<node-ip>:30001 (admin/admin)"
echo "   Prometheus: kubectl port-forward -n monitoring svc/prometheus 9090:9090"

# Deploy Logging Stack
echo ""
echo "📝 Deploying Logging Stack (ELK)..."
kubectl apply -f globalconf/logging/k8s/elasticsearch.yaml
wait_for_statefulset logging elasticsearch

kubectl apply -f globalconf/logging/k8s/logstash.yaml
wait_for_deployment logging logstash

kubectl apply -f globalconf/logging/k8s/kibana.yaml
wait_for_deployment logging kibana

echo "✅ Logging stack deployed successfully!"
echo "   Kibana: http://<node-ip>:30002"
echo "   Elasticsearch: kubectl port-forward -n logging svc/elasticsearch 9200:9200"

# Display status
echo ""
echo "📋 Deployment Status:"
echo ""
echo "Monitoring Namespace:"
kubectl get all -n monitoring
echo ""
echo "Logging Namespace:"
kubectl get all -n logging

echo ""
echo "🎉 All infrastructure deployed successfully!"
echo ""
echo "Next steps:"
echo "1. Access Grafana at http://<node-ip>:30001"
echo "2. Access Kibana at http://<node-ip>:30002"
echo "3. Update microservice logback configs to point to logstash.logging.svc.cluster.local:5001"
