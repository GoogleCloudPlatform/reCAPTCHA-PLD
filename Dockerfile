# Copyright 2024 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Build a .jar with maven and a custom JRE
FROM maven:3-eclipse-temurin-23 as build

ARG VERSION=0.0.1-SNAPSHOT
WORKDIR /build/
COPY pom.xml /build/
COPY src /build/src/

RUN mvn test

RUN mvn clean package -DskipTests

RUN cp target/pld-service-${VERSION}.jar target/app.jar

FROM gcr.io/distroless/java17-debian12 as pld-service

WORKDIR /app/

COPY --from=build /build/target/app.jar /app/

ENTRYPOINT [ "java", "-jar", "/app/app.jar", "--spring.ssl.bundle.jks.jks.key.password=${JKS_KEY_PASSWORD}", "--spring.ssl.bundle.jks.jks.keystore.password=${JKS_KEYSTORE_PASSWORD}"]
