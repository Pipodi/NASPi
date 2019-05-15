FROM arm64v8/jdk:11
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
CMD ["java","-jar app.jar"]
