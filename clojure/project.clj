(defproject red "2.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.10"]
                 [commons-daemon/commons-daemon "1.0.15"]
                 [environ "1.0.0"] ;;环境配置
                 [clj-time "0.7.0"]
                 [ring/ring "1.4.0"]
                 [ring/ring-json "0.3.1"]
                 [compojure "1.3.1"]
                 ;;[ch.qos.logback/logback-classic "1.0.13"]
                 [thrift-clj "0.2.1"] ;;thrift
                 ]
  :main red.core
  :java-source-paths ["../thrift/gen-java/"])
