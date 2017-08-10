(defproject nucredit "0.1.1"
  :url "http://nucredit.herokuapp.com"
  :dependencies
  [[org.clojure/clojure "1.8.0"]
   [org.clojure/clojure-contrib "1.2.0"]
   [ring/ring-jetty-adapter "1.6.2"]
   [ring-json-params "0.1.3"]
   [ring/ring-servlet "1.6.2"]
   [lein-heroku "0.5.3"]
   [compojure "1.6.0"]
   [clj-json "0.5.3"]
   [environ "1.1.0"]
   [clj-time "0.14.0"]]
  :dev-dependencies
  [[lein-run "1.0.0-SNAPSHOT"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "nucredit-standalone.jar"
  :profiles {:uberjar {:main nucredit.web, :aot :all}})