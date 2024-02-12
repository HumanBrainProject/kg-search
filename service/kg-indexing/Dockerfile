FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY target/*.jar kg-search-indexing.jar
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -jar /kg-search-indexing.jar
