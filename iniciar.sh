#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
command -v java >/dev/null || { echo "Java 17 necessario"; exit 1; }
command -v mvn >/dev/null || { echo "Maven necessario"; exit 1; }
command -v docker >/dev/null || { echo "Docker necessario"; exit 1; }
docker compose up -d --wait mysql
mvn -q -DskipTests compile exec:java
