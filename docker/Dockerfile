FROM eclipse-temurin:8u322-b06-jdk-alpine

ENV JAVA_HOME /opt/java/openjdk

RUN apk update && \
    apk add --no-cache openssl git go ca-certificates && \
    rm -rf /var/cache/apk/*

# RUN git clone https://github.com/FiloSottile/mkcert.git /tmp/mkcert && \
#     cd /tmp/mkcert && \
#     go build -ldflags "-X main.Version=$(git describe --tags)" && \
#    chmod +x mkcert

COPY ./simple-https-server-1.0-SNAPSHOT.jar /tmp/http/simple-https-server-1.0-SNAPSHOT.jar
COPY ./keystore.jks /tmp/cert/keystore.jks

EXPOSE 8443
ENTRYPOINT ["java", "-jar", "/tmp/http/simple-https-server-1.0-SNAPSHOT.jar", "/tmp/cert/keystore.jks", "JKS", "8443"]