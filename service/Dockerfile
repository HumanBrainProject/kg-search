FROM openjdk:11-jre-slim
VOLUME /tmp
COPY target/*.jar kg-search.jar
ENTRYPOINT exec java -jar /kg-search.jar