# CockroachDB Dynamic Certs Client

[![CircleCI](https://dl.circleci.com/status-badge/img/gh/udacity/cockroach-dynamic-certs/tree/master.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/udacity/cockroach-dynamic-certs/tree/master)

## Building the Image
```bash
docker build --no-cache -t timveil/cockroachdb-dynamic-certs:latest .
```

## Publishing the Image
```bash
docker push timveil/cockroachdb-dynamic-certs:latest
```

## Running the Image
```bash
docker run -it timveil/cockroachdb-dynamic-certs:latest
```

running the image with environment variables
```bash
docker run -p 9999:9999 \
    --env NODE_ALTERNATIVE_NAMES=localhost \
    -it timveil/cockroachdb-dynamic-certs:latest
```
