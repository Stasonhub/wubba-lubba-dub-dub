FROM openjdk:8

RUN \
  apt-get update && \
  apt-get install -y --no-install-recommends libfreetype6 libfreetype6-dev && \
  apt-get install -y --no-install-recommends libfontconfig1 libfontconfig1-dev

RUN \
  apt-get install -y --no-install-recommends libc6

VOLUME /tmp
ADD build/libs/airent-0.0.1-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]