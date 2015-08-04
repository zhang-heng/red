(defproject red "2.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.10"]
                 [commons-daemon/commons-daemon "1.0.15"]
                 ;;环境配置
                 [org.clojure/tools.logging "0.3.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jdmk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [environ "1.0.0"]
                 [clj-time "0.7.0"]
                 ;;web
                 [ring/ring "1.4.0"]
                 [ring/ring-json "0.3.1"]
                 [compojure "1.3.1"]
                 ;;thrift
                 [thrift-clj "0.2.1"]]
  :plugins [[lein-environ "1.0.0"]]

  :java-source-paths ["../thrift/gen-java/"]

  :global-vars {*warn-on-reflection* true
                *assert* true}

  :profiles {:dev        {:main red.core
                          :jvm-opts ["-Dlogfile.level=DEBUG,console"]
                          :env {:clj-env           :development
                                :subscribe-timeout 10
                                :gtsp-port         7748
                                :rest-port         8080}}
             :test       {}
             :uberjar    {:main red.server
                          :aot :all
                          :jvm-opts ["-Dlogfile.level=INFO,console,A3"]
                          :env {:clj-env :production}}})
