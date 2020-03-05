FROM adoptopenjdk/openjdk11:jre-11.0.5_10-debian
MAINTAINER David Kunzmann "david.kunzmann@epfl.ch"
RUN mkdir /app
COPY target/universal/stage/ /app/
WORKDIR /app
USER root
ENTRYPOINT ["bin/kg-service"]
CMD []