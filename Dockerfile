## BUILD STAGE ##
FROM maven:3.9-amazoncorretto-21-alpine AS build

COPY . /tmp/app
WORKDIR /tmp/app

# following nixpacks build command https://nixpacks.com/docs/providers/java
RUN mvn -DoutputFile=target/mvn-dependency-list.log -B -DskipTests clean dependency:list install

RUN mkdir -p /tmp/extracted && java -Djarmode=layertools -jar target/*jar extract --destination /tmp/extracted

## DISTROLESS IMAGE ##
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /tmp/app

COPY --from=build /tmp/extracted/dependencies /tmp/app/
COPY --from=build /tmp/extracted/spring-boot-loader /tmp/app/
COPY --from=build /tmp/extracted/snapshot-dependencies /tmp/app/
COPY --from=build /tmp/extracted/application /tmp/app/

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
