FROM arm32v7/openjdk:11.0.3-jre-stretch
COPY ./qemu-arm-static /usr/bin/qemu-arm-static
RUN apt-get update && apt-get install transmission-cli -y
COPY scripts/done.sh /done.sh
ARG JAR_FILE
ARG PATH_BASE
ARG PATH_DB
COPY ${JAR_FILE} app.jar
CMD java -Dpath.base=${PATH_BASE} -Dpath.db=${PATH_DB} -jar app.jar
