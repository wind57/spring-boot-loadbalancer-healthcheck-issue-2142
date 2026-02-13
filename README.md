# Spring Boot LoadBalancer Health Check Issue - Reproduction Steps

This repository reproduces an issue where **Spring Cloud LoadBalancer stops working when health check is enabled in Spring Boot 4**, while the same code works correctly with Spring Boot 3.

Issue report: https://github.com/spring-cloud/spring-cloud-kubernetes/issues/2142

## The Issue

When using Spring Cloud LoadBalancer with Kubernetes discovery and enabling the health check configuration:

```yaml
spring:
  cloud:
    loadbalancer:
      health-check:
        path:
          backend-service: /healthcheck
      configurations: health-check
```

- **Spring Boot 3.5.x + Spring Cloud 2025.0.1**: LoadBalancer works correctly ✅
- **Spring Boot 4.0.x + Spring Cloud 2025.1.1**: LoadBalancer fails to route requests ❌

## Prerequisites

- Docker
- Minikube
- kubectl

## Quick Start

### Reproduce the Issue with Spring Boot 4 (default)

```bash
cd reproduction/
./repro-start.sh
kubectl port-forward deployment/front-deployment 8080:8080
curl http://localhost:8080/
```

### Test with Spring Boot 3 (working version)

```bash
cd reproduction
./repro-start.sh --springboot3
kubectl port-forward deployment/front-deployment 8080:8080
curl http://localhost:8080/
```

## What the Script Does

The `repro-start.sh` script performs the following steps:

1. **Build backend application** - Compiles the backend service using Maven
2. **Build front application** - Compiles the front service using Maven
   - Uses `pom.xml` (Spring Boot 4) by default
   - Uses `pom2.xml` (Spring Boot 3) when `--springboot3` flag is provided
3. **Start Minikube** - Starts a local Kubernetes cluster if not already running
4. **Configure Docker** - Points Docker to Minikube's Docker daemon
5. **Build Docker images** - Creates container images for both applications
6. **Deploy to Kubernetes** - Applies all Kubernetes manifests and waits for deployments

## Manual Steps

### 1. Build Applications

```bash
# Build backend
cd ../backend && ./mvnw clean package -DskipTests

# Build front (Spring Boot 4)
cd ../front && ./mvnw clean package -DskipTests

# OR Build front (Spring Boot 3)
cd ../front && ./mvnw -f pom2.xml clean package -DskipTests
```

### 2. Start Minikube

```bash
minikube start
eval $(minikube docker-env)
```

### 3. Build Docker Images

```bash
docker build -t backend:latest -f dockerfiles/backend.Dockerfile ../backend
docker build -t front:latest -f dockerfiles/front.Dockerfile ../front
```

### 4. Apply Kubernetes Manifests

```bash
kubectl apply -f k8s/
```

### 5. Verify Deployment

```bash
kubectl get pods
kubectl get services
```

## Testing the Issue

Port forward the front service and make a request:

```bash
kubectl port-forward deployment/front-deployment 8080:8080 &
curl http://localhost:8080/
```

**Expected result with Spring Boot 3**: Returns `DONE` from backend  
**Actual result with Spring Boot 4**: Request fails / times out

## Viewing Logs

```bash
# Front application logs
kubectl logs -l app.kubernetes.io/component=front -f

# Backend application logs
kubectl logs -l app.kubernetes.io/component=backend -f
```

## Cleanup

Run the cleanup script:

```bash
cd reproduction/
./repro-stop.sh
```

Or manually:

```bash
cd reproduction/
kubectl delete -f k8s/
minikube stop
```

## Files

- `repro-start.sh` - Main reproduction script (supports `--springboot3` flag)
- `repro-stop.sh` - Cleanup script to remove Kubernetes resources
- `dockerfiles/` - Dockerfile definitions
  - `backend.Dockerfile` - Dockerfile for backend application
  - `front.Dockerfile` - Dockerfile for front application
- `k8s/` - Kubernetes manifests
  - `app-service-account.yaml` - ServiceAccount, Role, and RoleBinding for Kubernetes API access
  - `backend-deployment.yaml` - Backend deployment (1 replica)
  - `backend-service.yaml` - Backend headless service for service discovery
  - `front-deployment.yaml` - Front deployment (1 replica)

## Architecture

```
+-------------------------------------------------------------+
|                        Kubernetes                           |
|                                                             |
|  +------------------+         +------------------+          |
|  |      Front       |         |     Backend      |          |
|  |   Deployment     |         |   Deployment     |          |
|  |   (1 replica)    |         |   (1 replica)    |          |
|  +------------------+         +------------------+          |
|          |                            |                     |
|          |                            |                     |
|          v                            v                     |
|  +------------------+         +------------------+          |
|  |  Spring Cloud    |         | backend-service  |          |
|  |  LoadBalancer    |-------->|   (headless)     |          |
|  |   + K8s Client   |         | ClusterIP: None  |          |
|  +------------------+         +------------------+          |
|                                                             |
|  ServiceAccount: app-service-account                        |
|  Role: kubernetes-reader (list, watch, get on pods,         |
|        services, endpoints, endpointslices, etc.)           |
|                                                             |
+-------------------------------------------------------------+
```

## Version Details

| Component | Spring Boot 4 (Issue) | Spring Boot 3 (Working) |
|-----------|----------------------|-------------------------|
| Spring Boot | 4.0.2 | 3.5.10 |
| Spring Cloud | 2025.1.1 | 2025.0.1 |
| Java | 25 | 25 |
