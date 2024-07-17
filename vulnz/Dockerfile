FROM httpd:alpine

ARG BUILD_DATE
ARG BUILD_VERSION

ARG http_proxy
ARG https_proxy
ARG no_proxy

LABEL authors="derhecht,stevespringett,jeremylong"
LABEL maintainer="jeremy.long@gmail.com"
LABEL name="jeremylong/vulnz"
LABEL version=$BUILD_VERSION
LABEL org.label-schema.schema-version="1.0"
LABEL org.label-schema.build-date=$BUILD_DATE
LABEL org.label-schema.name="jeremylong/vulnz"
LABEL org.label-schema.description="Persist the data using the open-vulnerability-store."
LABEL org.label-schema.url="https://github.com/jeremylong/Open-Vulnerability-Project"
LABEL org.label-schema.vcs-url="https://github.com/jeremylong/Open-Vulnerability-Project"
LABEL org.label-schema.vendor="jeremylong"
LABEL org.label-schema.version=$BUILD_VERSION
LABEL org.label-schema.docker.cmd="docker run -it --rm --name mirror -e NVD_API_KEY=YOUR_API_KEY_HERE -p 80:80 jeremylong/vulnz"

ENV user=mirror
ENV BUILD_VERSION=$BUILD_VERSION
ENV JAVA_OPT=-Xmx2g

RUN apk update && \
    apk add --no-cache bash openjdk17 dcron nss supervisor tzdata && \
    addgroup -S "$user" && \
    adduser -S "$user" -G "$user" && \
    addgroup "$user" www-data && \
    addgroup www-data "$user" && \
    chown -R "$user":"$user" /usr/local/apache2/htdocs && \
    mkdir -p /var/log/supervisor && \
    rm -v /usr/local/apache2/htdocs/index.html

COPY ["/src/docker/supervisor/supervisord.conf", "/etc/supervisord.conf"]
COPY ["/src/docker/scripts/mirror.sh", "/mirror.sh"]
COPY ["/src/docker/crontab/mirror", "/etc/crontabs/mirror"]
COPY ["/src/docker/apache/mirror.conf", "/usr/local/apache2/conf"]
COPY ["/build/libs/vulnz-$BUILD_VERSION.jar", "/usr/local/bin/vulnz"]

RUN chmod +x /mirror.sh && \
    chown root:root /etc/crontabs/mirror && \
    chown mirror:mirror /mirror.sh && \
    chown mirror:mirror /usr/local/bin/vulnz 

# ensures we can log cron task is into stdout of docker
RUN ln -sf /proc/1/fd/1 /var/log/docker_out.log

VOLUME /usr/local/apache2/htdocs
WORKDIR /usr/local/apache2/htdocs
EXPOSE 80/tcp

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
