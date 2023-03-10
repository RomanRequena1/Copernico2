FROM openjdk:11 as stage0
LABEL snp-multi-stage="intermediate"
LABEL snp-multi-stage-id="7810c97e-a8e4-4f9c-8353-5759ff24260c"
WORKDIR /opt/docker
COPY opt /opt
USER root
RUN ["chmod", "-R", "u=rX,g=rX", "/opt/docker"]
RUN ["chmod", "u+x,g+x", "/opt/docker/bin/pcs"]

FROM openjdk:11
USER root
RUN id -u demiourgos728 1>/dev/null 2>&1 || (( getent group 0 1>/dev/null 2>&1 || ( type groupadd 1>/dev/null 2>&1 && groupadd -g 0 root || addgroup -g 0 -S root )) && ( type useradd 1>/dev/null 2>&1 && useradd --system --create-home --uid 1001 --gid 0 demiourgos728 || adduser -S -u 1001 -G root demiourgos728 ))
WORKDIR /opt/docker
COPY --from=stage0 --chown=demiourgos728:root /opt/docker /opt/docker
EXPOSE 2551 2552 2553 8081 8083 8084 8558 9095 5266
USER 1001:0
ENTRYPOINT ["/opt/docker/bin/pcs"]
CMD []
