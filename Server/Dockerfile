FROM debian:10 AS builder

RUN apt update && \
    apt install openjdk-11-jdk-headless gradle -y

COPY . /usr/src/amazonsserver

RUN cd /usr/src/amazonsserver/Server && \
    gradle clean && \
    gradle jar



FROM debian:10

EXPOSE 33100
ENV AMAZONS_ADDRESS=localhost
ENV AMAZONS_DEBUG=false
ENV AMAZONS_TOKEN=

RUN apt update && \
    apt install openjdk-11-jre-headless -y

COPY ./Server/deploy/amazonsserver-docker-init.sh /usr/local/bin
RUN chmod +x /usr/local/bin/amazonsserver-docker-init.sh

COPY --from=builder /usr/src/amazonsserver/Server/build/libs/Server.jar /usr/local/bin/amazonsserver.jar

CMD ["/usr/local/bin/amazonsserver-docker-init.sh"]