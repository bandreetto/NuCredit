(ns web-api
  (:use compojure.core)
  (:use ring.middleware.json-params)
  (:require [clj-json.core :as json]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [compojure.handler :refer [site]]
            [services])
  (:import (org.codehaus.jackson JsonParseException))
  (:import (clojure.contrib.condition Condition)))

(def error-codes
  {:invalid 400
   :not-found 404})

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Welcome to NuCredit Platform"})

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)})

(defroutes handler
           (GET "/" []
             (splash))
           (PUT "/new-account" [name]
             (json-response (services/create-account name))))

(defn wrap-error-handling [handler]
  (fn [req]
    (try
      (or (handler req)
          (json-response {"error" "resource not found"} 404))
      (catch JsonParseException e
        (json-response {"error" "malformed json"} 400))
      (catch Condition e
        (let [{:keys [type message]} (meta e)]
          (json-response {"error" message} (error-codes type)))))))

(def app
  (-> handler
      wrap-json-params
      wrap-error-handling))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))