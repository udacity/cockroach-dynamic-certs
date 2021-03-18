# CockroachDB Dynamic Certs Client


## Building the Image
```bash
./mvnw clean package
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
docker run \
    --env COCKROACH_HOST=localhost:26257 \
    --env COCKROACH_INSECURE=true \
    --env DATABASE_NAME=test \
    --env COCKROACH_INIT=true \
    -it timveil/cockroachdb-dynamic-certs:latest
```