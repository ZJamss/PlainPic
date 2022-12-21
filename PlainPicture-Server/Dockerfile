FROM java:8
MAINTAINER ZJAMSS
ADD target/PlainPicture-Server-0.0.1-SNAPSHOT.jar app.jar
RUN bash -c 'mkdir /img && chmod 777 /img'
ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8080
