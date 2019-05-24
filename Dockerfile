FROM arm32v7/openjdk:11.0.3-jre-stretch
COPY ./qemu-arm-static /usr/bin/qemu-arm-static
RUN apt-get update && apt-get install transmission-daemon -y && apt-get install transmission-cli -y
COPY settings.json /etc/transmission-daemon/settings.json
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
CMD java -Dpath.base=/media/nas1 -jar app.jar
