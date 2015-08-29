FROM clojure

COPY clojure/ /usr/src/app
WORKDIR /usr/src/app

RUN lein deps

CMD ["lein", "run"]
