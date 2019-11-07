FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD klinemongodb.jar klinemongodb.jar
RUN sh -c 'touch /klinemongodb.jar'
EXPOSE 8008
ENV JAVA_OPTS="-server -Xms2048m -Xmx2048m -Xss512k" PROFILE="deploy"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Duser.timezone=GMT+08 -Dspring.profiles.active=$PROFILE -jar /klinemongodb.jar" ]

