# Distributed Task Scheduling System

### Technologies:

- Micronaut: For building the API and worker services.
- Kafka: For distributing task messages to worker nodes.
- Database: Use PostgreSQL or MySQL to store task details and their statuses.
- Quartz Scheduler: To handle task scheduling.


### Key Features:
#### Task Submission API:
Users can submit tasks with a specified execution time.

Store tasks in a database with scheduling information.

#### Task Distributor:
Periodically checks for tasks ready for execution.

Publishes tasks to Kafka topics for worker nodes to pick up.

#### Worker Nodes:
Microservices that consume tasks from Kafka and execute them.

Report task completion or failure back to the Task Submission API.

#### Task Status Monitoring:
Provide an API or UI to check the status of scheduled and completed tasks.


### needed commands

```
kubectl create namespace dtss
```

#### API

```
make

docker run -p 8080:8080 dtss-api:0.1

curl -X POST -d '{"name":"eventTest"}' -H "Content-Type: application/json" http://localhost:8080/event
```

#### Red panda
```
kubectl port-forward svc/redpanda-service 9092:9092 -n dtss

rpk topic create events --brokers localhost:9092

echo "Hello, Redpanda!" | rpk topic produce events --brokers localhost:9092

rpk topic consume events --brokers localhost:9092
```

### CouchDB

TODO: setup db

```
kubectl port-forward svc/couchdb-service 5984:5984 -n dtss

```