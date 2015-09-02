FROM zhhkay/u64-j7-t90
COPY . /usr/src
WORKDIR /usr/src/thrift
RUN ./gen
WORKDIR /usr/src/cpp
RUN make x64 -j4 PLAT=64
WORKDIR /usr/src/clojure
RUN lein uberjar
WORKDIR /usr/app
RUN cp /usr/src/clojure/target/red-2.0.0-SNAPSHOT-standalone.jar .
RUN rm -rf /usr/src
CMD ["java", "-jar", "red-2.0.0-SNAPSHOT-standalone.jar"]
