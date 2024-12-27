FROM maven:3.9.9-amazoncorretto-21-debian as build

RUN apt-get update && apt-get install -y binutils

# prepare runtime
WORKDIR /build
RUN jlink --add-modules ALL-MODULE-PATH --output runtime --no-header-files --no-man-pages --compress=2 --strip-debug

# build all dependencies for offline use and cache them
COPY pom.xml ./
RUN mvn dependency:go-offline

# copy all other files
COPY src ./src
COPY res ./res
COPY rules.xml ./rules.xml

# build
RUN mvn package

FROM debian:bookworm-slim
WORKDIR /sorter
COPY --from=build /build/runtime runtime
COPY --from=build /build/target/*.jar ./sorter.jar
ENTRYPOINT runtime/bin/java -jar sorter.jar & wait
