(defproject metrics-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler metrics-server.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-json "0.4.0"]
                        [me.raynes/fs "1.4.6"]
                        [me.raynes/conch "0.8.0"]
                        [clj-yaml "0.4.0"]
                        [org.clojure/data.xml "0.0.8"]
                        [org.clojure/data.zip "0.1.1"]
                        [ring-mock "0.1.5"]]}})
