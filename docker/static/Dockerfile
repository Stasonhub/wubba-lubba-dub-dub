FROM nginx

RUN rm /etc/nginx/conf.d/default.conf

COPY build/resources/main/static /static

COPY docker/static/nginx.conf /etc/nginx/conf.d
