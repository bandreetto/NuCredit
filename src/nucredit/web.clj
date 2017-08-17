(ns nucredit.web
  (:use compojure.core)
  (:use ring.middleware.json-params)
  (:require [cheshire.core :as json]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [compojure.handler :refer [site]]
            [nucredit.services :as services])
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
           (GET "/get-balance" [account-number]
             (json-response (services/get-balance account-number)))
           (GET "/get-statement" [account-number]
             (json-response (services/get-statement account-number)))
           (GET "/get-debt-periods" [account-number]
             (json-response (services/get-debt-periods account-number)))
           (PUT "/create-account" [name]
             (json-response (services/create-account name)))
           (PUT "/operate" [party
                            counter-party
                            amount
                            offset]
             (json-response (services/operate party
                                              counter-party
                                              (BigDecimal. amount)
                                              offset))))

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