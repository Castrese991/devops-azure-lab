#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
# Containerized Maven avoids requiring a local JDK/Maven installation.
docker run --rm -v "$PWD:/workspace" -w /workspace -v devops-lab-maven:/root/.m2 \
 maven:3.9.12-eclipse-temurin-17 mvn -B -ntp verify
