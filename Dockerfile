## BUILD STAGE ##
FROM maven:3.9-amazoncorretto-21-alpine AS build
WORKDIR /tmp/app

# copy outside cache
COPY settings.xml /

# download and cache dependencies
COPY ./pom.xml /tmp/app
RUN --mount=type=secret,id=GITHUB_USERNAME,env=GITHUB_USERNAME,required=true \
    --mount=type=secret,id=GITHUB_TOKEN,env=GITHUB_TOKEN,required=true \
    --mount=type=cache,target=/root/.m2 \
    cp /settings.xml /root/.m2 && \
    mvn dependency:go-offline

# build the app
COPY . /tmp/app

# token are mount as Docker secret mounts, see: https://docs.docker.com/reference/dockerfile/#run---mounttypesecret
# application following nixpacks build command, see: https://nixpacks.com/docs/providers/java
RUN     --mount=type=cache,target=/root/.m2 \
    mvn -DoutputFile=target/mvn-dependency-list.log -B -DskipTests install

# use layertools for build cache
RUN mkdir -p /tmp/extracted && java -Djarmode=layertools -jar target/*jar extract --destination /tmp/extracted

## DISTROLESS IMAGE ##
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /tmp/app

COPY --from=build /tmp/extracted/dependencies /tmp/app/
COPY --from=build /tmp/extracted/spring-boot-loader /tmp/app/
COPY --from=build /tmp/extracted/snapshot-dependencies /tmp/app/
COPY --from=build /tmp/extracted/application /tmp/app/

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
