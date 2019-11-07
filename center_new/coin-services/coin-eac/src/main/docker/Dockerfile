FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD coin-eac.jar app.jar
RUN sh -c 'touch /app.jar'
EXPOSE 8005
ENV JAVA_OPTS="" PROFILE="deploy"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=$PROFILE -jar /app.jar" ]

