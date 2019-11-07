FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD eureka.jar eureka.jar
RUN sh -c 'touch /eureka.jar'
EXPOSE 8001
ENV JAVA_OPTS="" PROFILE="deploy"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=$PROFILE -jar /eureka.jar" ]

