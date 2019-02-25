
ifdef tag
	TAG=$(tag)
else
	TAG=latest
endif

ps:
	docker ps -a

images:
	docker images | grep -v REPOSITORY | sort

build-incremental:
	s2i build -e PROFILE=docker -e WAR_NAME=paas.server.war -e INCREMENTAL=true --incremental https://git.oschina.net/ruffylining/paas.git 139.219.239.226/s2i/s2i-tomcat:8.5-jdk8-mvn3.5.0 139.219.239.226/paas/paas-server:$(TAG) -r master
	s2i build -e PROFILE=docker -e WAR_NAME=paas.web.war -e INCREMENTAL=true --incremental https://git.oschina.net/ruffylining/paas.git 139.219.239.226/s2i/s2i-tomcat:8.5-jdk8-mvn3.5.0 139.219.239.226/paas/paas-web:$(TAG) -r master

build:
	s2i build -e PROFILE=docker -e WAR_NAME=paas.server.war -e INCREMENTAL=false https://git.oschina.net/ruffylining/paas.git 139.219.239.226/s2i/s2i-tomcat:8.5-jdk8-mvn3.5.0 139.219.239.226/paas/paas-server:$(TAG) -r master
	s2i build -e PROFILE=docker -e WAR_NAME=paas.web.war -e INCREMENTAL=false https://git.oschina.net/ruffylining/paas.git 139.219.239.226/s2i/s2i-tomcat:8.5-jdk8-mvn3.5.0 139.219.239.226/paas/paas-web:$(TAG) -r master

tag:
	docker tag 139.219.239.226/paas/paas-web:$(TAG) 139.219.239.226/paas/paas-web:latest
	docker tag 139.219.239.226/paas/paas-server:$(TAG) 139.219.239.226/paas/paas-server:latest

push:
	docker push 139.219.239.226/paas/paas-server:$(TAG)
	docker push 139.219.239.226/paas/paas-web:$(TAG)

pull:
	docker pull 139.219.239.226/paas/paas-server:$(TAG)
	docker pull 139.219.239.226/paas/paas-web:$(TAG)

bash-paas-server:
	docker-compose exec paas-server bash

bash-paas-web:
	docker-compose exec paas-web bash

log-paas-server:
	docker-compose logs -f paas-server

log-paas-web:
	docker-compose logs -f paas-web

up:
	mkdir -p ./mysql/data
	docker-compose up -d

up-init:
	rm -rf ./mysql/data && mkdir -p ./mysql/data
	docker-compose up -d

up-test:
	mkdir -p ./mysql/data
	docker-compose -f docker-compose-test.yml up -d

up-paas:
	mkdir -p ./mysql/data
	docker-compose -f docker-compose-paas.yml up -d

start:
	docker-compose start

stop:
	docker-compose stop

rm:
	docker-compose rm -f
