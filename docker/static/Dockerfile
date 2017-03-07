FROM nginx

RUN \
  apt-get update && \
  apt-get install -y --no-install-recommends certbot -t jessie-backports

RUN rm /etc/nginx/conf.d/default.conf

COPY build/resources/main/static /static

COPY docker/static/nginx.conf /etc/nginx/conf.d
