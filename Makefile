
# VARIABLES
MAVEN_PROJ_LIST := kafka-webview-ui
MAVEN_SKIP := -DskipTests=true -DskipCheckStyle=true -DskipLicenseCheck=true
RELEASE_VERSION := 2.5.0


# CONFIG
.PHONY: help print-variables
.DEFAULT_GOAL := help


# ACTIONS

## BUILD

### Kafka-WebView only

compile :		## Compile Kafka-WebView project only
	mvn clean compile -pl $(MAVEN_PROJ_LIST)

test :		## Test Kafka-WebView project only
	mvn clean test -pl $(MAVEN_PROJ_LIST)

package :		## Build Kafka-WebView jar only
	mvn clean package -pl $(MAVEN_PROJ_LIST)

package-skip :		## Build Kafka-WebView jar only, skipping tests and checks
	mvn clean package -pl $(MAVEN_PROJ_LIST) $(MAVEN_SKIP)

### All

compile-all :		## Compile all projects
	mvn clean compile

test-all :		## Test all projects
	mvn clean test

package-all :		## Build all jars
	mvn clean package

package-all-skip :		## Build all jars, skipping tests and checks
	mvn clean package $(MAVEN_SKIP)

## RUN

run :		## Build and run Kafka-WebView only using Maven
	export SPRING_PROFILES_ACTIVE="dev"; \
	mvn spring-boot:run -pl $(MAVEN_PROJ_LIST) $(MAVEN_SKIP)

run-jar : package-skip		## Build and run Kafka-WebView only using jar
	export SPRING_PROFILES_ACTIVE="dev"; \
	java $(JVM_OPTS) $(MEM_OPTS) $(JAVA_OPTS) -jar kafka-webview-ui-*.jar

## DOCKER

docker-build :		## Build Docker image
	docker build -t sourcelaborg/kafka-webview .

docker-push :		## Push Docker image to Docker Hub
	docker push sourcelaborg/kafka-webview:latest

docker-tag-release :		## Tag Docker image with version $(RELEASE_VERSION)
	docker tag sourcelaborg/kafka-webview sourcelaborg/kafka-webview:$(RELEASE_VERSION)

docker-push-release :		## Push Docker image version $(RELEASE_VERSION)
	docker push sourcelaborg/kafka-webview:$(RELEASE_VERSION)

## SSL

# TODO

## HELPERS

help :		## Help (default action)
	@echo ""
	@echo "*** \033[33mMakefile help\033[0m ***"
	@echo ""
	@echo "Targets list:"
	@grep -E '^[a-zA-Z_-]+ :.*?## .*$$' $(MAKEFILE_LIST) | sort -k 1,1 | awk 'BEGIN {FS = ":.*?## "}; {printf "\t\033[36m%-30s\033[0m %s\n", $$1, $$2}'
	@echo ""

print-variables :		## Print variables values
	@echo ""
	@echo "*** \033[33mMakefile variables\033[0m ***"
	@echo ""
	@echo "- - - makefile - - -"
	@echo "MAKE: $(MAKE)"
	@echo "MAKEFILES: $(MAKEFILES)"
	@echo "MAKEFILE_LIST: $(MAKEFILE_LIST)"
	@echo "- - -"
	@echo "MAVEN_PROJ_LIST: $(MAVEN_PROJ_LIST)"
	@echo "MAVEN_SKIP: $(MAVEN_SKIP)"
	@echo "RELEASE_VERSION: $(RELEASE_VERSION)"
	@echo ""
