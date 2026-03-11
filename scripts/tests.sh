#!/bin/bash
set -e

echo "Running tests in Docker container..."
docker build --rm -t subito-test -f ../DockerfileTest ../

echo "✅ Tests passed! Extracting coverage report..."
# Copy JaCoCo report dal container al host
docker create --name temp-container subito-test
docker cp temp-container:/app/target/site/jacoco ./jacoco-report
docker rm temp-container
docker rmi subito-test

echo "📊 Coverage report available at: ./jacoco-report/index.html"

