FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD user.jar user.jar
RUN sh -c 'touch /user.jar'
EXPOSE 8002
ENV JAVA_OPTS="" PROFILE="deploy"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=$PROFILE -jar /user.jar" ]

