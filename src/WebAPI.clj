(ns .WebAPI
  (:use compojure.core)
  (:use ring.middleware.json-params)
  (:require [clj-json.core :as json]
            [ring.adapter.jetty :as jetty]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)})

(defroutes handler
           (GET "/" []
             (json-response {"hello" "world"}))
           (PUT "/" [name]
             (json-response {"Hello" name})))

(def app
  (-> handler
      wrap-json-params))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))