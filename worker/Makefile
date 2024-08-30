# Variables
APP_NAME = dtss-worker
JAR_NAME = worker
VERSION = 0.1
JAR_FILE = build/libs/$(JAR_NAME)-$(VERSION)-all.jar
DOCKER_IMAGE = $(APP_NAME):$(VERSION)

# Default target
all: build-jar build-docker

# Build the JAR file using Gradle
build-jar:
	./gradlew clean build

# Build the Docker image
build-docker: $(JAR_FILE)
	docker build -t $(DOCKER_IMAGE) .

# Clean the project (optional)
clean:
	./gradlew clean

# Phony targets
.PHONY: all build-jar build-docker clean