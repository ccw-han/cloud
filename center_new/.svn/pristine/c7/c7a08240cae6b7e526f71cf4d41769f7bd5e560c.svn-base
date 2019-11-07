FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD javaapi.jar javaapi.jar
RUN sh -c 'touch /javaapi.jar'
RUN mkdir -p /usr/aaa/dump
EXPOSE 8009
ENV JAVA_OPTS=" $JAVA_OPTS -server -Xms3G -Xmx8G -Xss256k -XX:PermSize=128m -XX:MaxPermSize=128m -XX:+UseParallelOldGC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/usr/aaa/dump -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:/usr/aaa/dump/heap_trace.txt -XX:NewSize=1G -XX:MaxNewSize=1G " PROFILE="deploy"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -Duser.timezone=GMT+08 -Dspring.profiles.active=$PROFILE -jar /javaapi.jar" ]

