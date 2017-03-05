FROM openjdk:8

RUN \
  apt-get update && \
  apt-get install -y --no-install-recommends libfreetype6 libfreetype6-dev && \
  apt-get install -y --no-install-recommends libfontconfig1 libfontconfig1-dev

RUN \
  wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && \
  echo "deb http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google.list && \
  apt-get update && \
  apt-get install -y google-chrome-stable xvfb && \
  rm -rf /var/lib/apt/lists/*

VOLUME /tmp
ADD build/libs/airent-0.0.1-SNAPSHOT.jar app.jar
ADD docker/main/run_app.sh run_app.sh

RUN sh -c 'touch /app.jar'
RUN sh -c 'touch /run_app.sh'

ENV JAVA_OPTS=""

CMD sh /run_app.sh