FROM adoptopenjdk:11-jre-hotspot
MAINTAINER David Kunzmann "david.kunzmann@epfl.ch"
RUN mkdir /app
COPY target/universal/stage/ /app/
WORKDIR /app
USER root
ENTRYPOINT ["bin/kg-service"]
CMD []