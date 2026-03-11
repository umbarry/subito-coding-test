#!/bin/bash
set -e

echo "Running tests in Docker container..."
docker build --rm -t subito-test -f ../DockerfileTest ../
docker rmi subito-test
echo "Tests completed successfully!"

