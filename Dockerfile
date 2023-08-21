# official Scala base image
FROM hseeberger/scala-sbt:17.0.2_1.6.2_3.1.1

# working directory --> container
WORKDIR /app

COPY . /app

# nginx tmp
RUN mkdir -p /var/nginx/client_body_temp \
    /var/nginx/cache \
    /var/nginx/proxy_temp \
    /var/nginx/fastcgi_temp \
    /var/nginx/uwsgi_temp \
    /var/nginx/scgi_temp

# chown
RUN chown -R nobody:nogroup /var/nginx/client_body_temp \
    /var/nginx/cache \
    /var/nginx/proxy_temp \
    /var/nginx/fastcgi_temp \
    /var/nginx/uwsgi_temp \
    /var/nginx/scgi_temp

STOPSIGNAL SIGQUIT
