FROM debian:10 AS builder

RUN apt update && \
    apt install openjdk-11-jdk-headless gradle -y

COPY . /usr/src/amazonsserver

RUN cd /usr/src/amazonsserver/KISpieler && \
    gradle clean && \
    gradle jar



FROM debian:10

EXPOSE 33098
ENV AMAZONS_ADDRESS=localhost
ENV AMAZONS_DEBUG=false
ENV AMAZONS_TOKEN=

RUN apt update && \
    apt install openjdk-11-jre-headless -y

COPY ./KISpieler/deploy/amazonskispieler-docker-init.sh /usr/local/bin
RUN chmod +x /usr/local/bin/amazonskispieler-docker-init.sh

COPY --from=builder /usr/src/amazonsserver/KISpieler/build/libs/KISpieler.jar /usr/local/bin/amazonskispieler.jar

CMD ["/usr/local/bin/amazonskispieler-docker-init.sh"]