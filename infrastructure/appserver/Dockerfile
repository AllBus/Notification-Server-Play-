# appserver for https://  ???
#
# VERSION               0.0.1

FROM      ubuntu:14.04

# make sure the package repository is up to date
RUN echo "deb http://archive.ubuntu.com/ubuntu trusty main universe" > /etc/apt/sources.list ;\
	apt-get update ;\
    apt-get install -y software-properties-common ;\
    apt-get update ;\
    add-apt-repository ppa:openjdk-r/ppa ;\
    apt-get update
RUN apt-get install -y --no-install-recommends git openjdk-8-jdk htop unzip wget

ADD /notificator-server-0.2.zip /
RUN unzip notificator-server-0.2.zip

VOLUME /var/kos/notifity

CMD notificator-1.0-SNAPSHOT/bin/notificator -DapplyEvolutions.default=true

EXPOSE 9000