FROM openjdk:8

RUN \
  apt-get update && \
  apt-get install -y --no-install-recommends libfreetype6 libfreetype6-dev && \
  apt-get install -y --no-install-recommends libfontconfig1 libfontconfig1-dev && \
  apt-get install -y --no-install-recommends libc6


RUN \
 apt-get install -y  --no-install-recommends ca-certificates \
                                             bzip2 \
                                             libfontconfig