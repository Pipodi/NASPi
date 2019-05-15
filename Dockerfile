FROM arm64v8/openjdk:11.0.3-jre-stretch
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
CMD ["java","-jar app.jar"]
