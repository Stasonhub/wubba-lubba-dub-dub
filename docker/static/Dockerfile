FROM nginx

RUN \
  echo "deb http://ftp.debian.org/debian jessie-backports main" > /etc/apt/sources.list.d/backports.list && \
  apt-get update && \
  apt-get install -y certbot -t jessie-backports

RUN rm /etc/nginx/conf.d/default.conf
RUN mkdir /challenge

COPY build/resources/main/static /static
COPY docker/static/nginx.conf /etc/nginx/conf.d
