FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD robot.jar robot.jar
RUN sh -c 'touch /robot.jar'
EXPOSE 8007
ENV JAVA_OPTS="-server -Xms2048m -Xmx2048m -Xss512k" PROFILE="deploy"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=$PROFILE -jar /robot.jar" ]

