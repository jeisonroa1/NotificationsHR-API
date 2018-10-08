FROM maven:3-jdk-8-alpine
EXPOSE 8080
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app
CMD ["java", "-jar", "target/notifications-0.0.1-SNAPSHOT.jar"]
