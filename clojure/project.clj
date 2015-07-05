(defproject red "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.10"]
                 [clj-time "0.7.0"]
                 ;;[ch.qos.logback/logback-classic "1.0.13"]
                 [environ "1.0.0"]    ;;环境配置
                 [thrift-clj "0.2.1"] ;;thrift
                 ]
  :main red.core
  :java-source-paths ["../thrift/gen-java/"])
