# Reference from Spring Boot with Docker - https://spring.io/guides/gs/spring-boot-docker/

FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
VOLUME /tmp/keystores
ADD target/passkeeper-0.0.1-SNAPSHOT.jar passkeeper.jar
RUN sh -c 'touch /passkeeper.jar'
ENV JAVA_OPTS="-Dsecurity.keystore.location=/tmp/keystores"

# https://wiki.apache.org/tomcat/HowTo/FasterStartUp#Entropy_Source
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /passkeeper.jar" ]
