#!/bin/bash
set -e

echo "Running tests in Docker container..."
docker build -t subito-test .
docker run --rm subito-test mvn test
echo "Tests completed successfully!"

