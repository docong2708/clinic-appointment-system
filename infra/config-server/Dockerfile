FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY target/*.jar ./

RUN rm -f *-plain.jar && mv *.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]