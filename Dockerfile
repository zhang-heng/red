FROM java:8u66
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY clojure/target/red-2.0.0-SNAPSHOT-standalone.jar /usr/src/app/
CMD ["java", "-jar", "red-2.0.0-SNAPSHOT-standalone.jar"]
