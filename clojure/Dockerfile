#  docker rm $(docker ps -a -q)

#  docker rmi $(docker images | grep "^<none>" | awk "{print $3}")

#FROM clojure
#RUN mkdir -p /usr/src/app
#WORKDIR /usr/src/app
#COPY project.clj /usr/src/app/
#RUN lein deps
#RUN rm project.clj

FROM index.alauda.cn/zhangheng/red:cljbuild
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN lein deps
CMD ["lein", "run"]
