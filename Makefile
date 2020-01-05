
# VARIABLES
MAVEN_PROJ_LIST = kafka-webview-ui
MAVEN_SKIP = -DskipTests=true -DskipCheckStyle=true -DskipLicenseCheck=true

# Available values: local | release
DOCKERFILE_PREFIX = local

# If DOCKERFILE_PREFIX set as 'release', please properly specify also following variables
WEBVIEW_RELEASE_VERSION = 2.5.0
WEBVIEW_RELEASE_SHA1 = 00a8db474ba2c584c5a473c8ca9acbd0259c01de
WEBVIEW_RELEASE_URL = https://github.com/SourceLabOrg/kafka-webview/releases/download/v$(WEBVIEW_RELEASE_VERSION)/kafka-webview-ui-$(WEBVIEW_RELEASE_VERSION)-bin.zip



# CONFIG
.PHONY: help print-variables
.DEFAULT_GOAL := help



# ACTIONS

## BUILD

### Kafka-WebView only

clean :		## Clean Kafka-WebView project only
	mvn clean -pl $(MAVEN_PROJ_LIST)

compile :		## Compile Kafka-WebView project only
	mvn clean compile -pl $(MAVEN_PROJ_LIST)

test :		## Test Kafka-WebView project only
	mvn clean test -pl $(MAVEN_PROJ_LIST)

package :		## Build Kafka-WebView jar only
	mvn clean package -pl $(MAVEN_PROJ_LIST)

package-skip :		## Build Kafka-WebView jar only, skipping tests and checks
	mvn clean package -pl $(MAVEN_PROJ_LIST) $(MAVEN_SKIP)

### All

clean-all :		## Clean all projects
	mvn clean compile

compile-all :		## Compile all projects
	mvn clean compile

test-all :		## Test all projects
	mvn clean test

package-all :		## Build all jars
	mvn clean package

package-all-skip :		## Build all jars, skipping tests and checks
	mvn clean package $(MAVEN_SKIP)

## DOCKER

docker-build :		## Build Docker image
ifeq ($(DOCKERFILE_PREFIX), local)
	docker build \
		-f $(DOCKERFILE_PREFIX).Dockerfile \
		-t sourcelaborg/kafka-webview \
		.
else
	docker build \
		-f $(DOCKERFILE_PREFIX).Dockerfile \
		-t sourcelaborg/kafka-webview \
		--build-arg WEBVIEW_VER=$(WEBVIEW_RELEASE_VERSION) \
		--build-arg WEBVIEW_SHA1=$(WEBVIEW_RELEASE_SHA1) \
		--build-arg WEBVIEW_URL=$(WEBVIEW_RELEASE_URL) \
		.
endif

docker-push :		## Push Docker image to Docker Hub
	docker push sourcelaborg/kafka-webview:latest

docker-tag-release :		## Tag Docker image with version $(RELEASE_VERSION)
	docker tag sourcelaborg/kafka-webview sourcelaborg/kafka-webview:$(RELEASE_VERSION)

docker-push-release :		## Push Docker image version $(RELEASE_VERSION)
	docker push sourcelaborg/kafka-webview:$(RELEASE_VERSION)

## RUN

run :		## Build and run Kafka-WebView only using Maven
	export SPRING_PROFILES_ACTIVE="dev"; \
	mvn spring-boot:run -pl $(MAVEN_PROJ_LIST) $(MAVEN_SKIP)

jar-run : package-skip		## Build and run Kafka-WebView only using jar
	export SPRING_PROFILES_ACTIVE="dev"; \
	java $(JVM_OPTS) $(MEM_OPTS) $(JAVA_OPTS) -jar kafka-webview-ui-*.jar

docker-run : docker-build		## Build Docker image and run Kafka-WebView in container
	docker run -ti --rm --name kafka-webview \
		-p 8080:8080 -p 9090:9090 \
		sourcelaborg/kafka-webview

docker-run-daemon : docker-build		## Build Docker image and run Kafka-WebView in container as daemon
	docker run -d --rm --name kafka-webview \
		-p 8080:8080 -p 9090:9090 \
		sourcelaborg/kafka-webview

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
	@echo "DOCKERFILE_PREFIX: $(DOCKERFILE_PREFIX)"
	@echo "WEBVIEW_RELEASE_VERSION: $(WEBVIEW_RELEASE_VERSION)"
	@echo "WEBVIEW_RELEASE_SHA1: $(WEBVIEW_RELEASE_SHA1)"
	@echo ""
