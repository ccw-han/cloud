FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD target/coin-bcc.jar app.jar
RUN sh -c 'touch /app.jar'
EXPOSE 8002
ENV JAVA_OPTS="" PROFILE="deploy"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Duser.timezone=GMT+08 -Dspring.profiles.active=$PROFILE -jar /app.jar" ]
