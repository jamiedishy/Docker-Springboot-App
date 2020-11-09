FROM maven:3.6.3-jdk-8 AS build
COPY /demo/src /home/app/src
COPY /demo/pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

FROM openjdk:8
COPY --from=build /home/app/target/demo-0.0.1-SNAPSHOT.jar /usr/src/myapp/demo.jar
EXPOSE 5000
ENTRYPOINT ["java","-jar","/usr/src/myapp/demo.jar"]