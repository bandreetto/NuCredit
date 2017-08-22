(ns nucredit.tests.integrated-tests
  (:require [clj-http.client :as client]
            [clojure.test :refer :all]
            [cheshire.core :as json]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.format :as f]
            [nucredit.web :refer [-main]]))



(deftest create-account-test
  (def server (-main 5001))
  (let [response (client/post "http://localhost:5001/create-account"
                             {:body (json/generate-string {:name "NuBank"})
                              :content-type :json})]
    (is (= (response :status) 200))
    (is (= ((json/parse-string (response :body) true) :name) "NuBank")))
  (let [response (client/post "http://localhost:5001/create-account"
                              {:body (json/generate-string {:name "BTG"})
                               :content-type :json})]
    (is (= (response :status) 200))
    (is (= ((json/parse-string (response :body) true) :name) "BTG"))))


(defn create-scenario-account [port]
  (let [response (client/post (str "http://localhost:" port "/create-account")
                {:body (json/generate-string {:name "NuBank"})
                 :content-type :json})]
    ((json/parse-string (response :body) true) :account-number)))


(deftest operate-test
  (def server (-main 5002))
  (let [id (create-scenario-account 5002)
        response1 (client/post "http://localhost:5002/operate"
                               {:body (json/generate-string {:party id
                                                             :counter-party nil
                                                             :amount 1000
                                                             :offset nil})
                                :content-type :json})
        response2 (client/post "http://localhost:5002/operate"
                               {:body (json/generate-string {:party id
                                                             :counter-party "Amazon"
                                                             :amount -3.34
                                                             :offset 1})
                                :content-type :json})
        response3 (client/post "http://localhost:5002/operate"
                               {:body (json/generate-string {:party id
                                                             :counter-party "Uber"
                                                             :amount -45.23
                                                             :offset 1})
                                :content-type :json})
        response4 (client/post "http://localhost:5002/operate"
                               {:body (json/generate-string {:party id
                                                             :counter-party nil
                                                             :amount -180
                                                             :offset 2})
                                :content-type :json})]
    (is (= (response1 :status) 200))
    (is (= (json/parse-string (response1 :body) true) {:name "NuBank" :balance 1000}))
    (is (= (response2 :status) 200))
    (is (= (json/parse-string (response2 :body) true) {:name "NuBank" :balance 996.66}))
    (is (= (response3 :status) 200))
    (is (= (json/parse-string (response3 :body) true) {:name "NuBank" :balance 951.43}))
    (is (= (response4 :status) 200))
    (is (= (json/parse-string (response4 :body) true) {:name "NuBank" :balance 771.43}))))


(defn create-scenario-operations [port account-number]
  (client/post (str "http://localhost:" port "/operate")
               {:body (json/generate-string {:party account-number
                                             :counter-party nil
                                             :amount 1000
                                             :offset 0})
                :content-type :json})
  (client/post (str "http://localhost:" port "/operate")
               {:body (json/generate-string {:party account-number
                                             :counter-party "Amazon"
                                             :amount -3.34
                                             :offset 1})
                :content-type :json})
  (client/post (str "http://localhost:" port "/operate")
               {:body (json/generate-string {:party account-number
                                             :counter-party "Uber"
                                             :amount -45.23
                                             :offset 1})
                :content-type :json})
  (client/post (str "http://localhost:" port "/operate")
               {:body (json/generate-string {:party account-number
                                             :counter-party nil
                                             :amount -180
                                             :offset 2})
                :content-type :json}))


(deftest get-balance-test
  (def server (-main 5003))
  (let [id (create-scenario-account 5003)]
    (create-scenario-operations 5003 id)
    (let [response (client/get (str "http://localhost:5003/get-balance/" id) {:accept :json})]
      (is (= (response :status) 200))
      (is (= (json/parse-string (response :body) true) {:name "NuBank" :balance 771.43})))))

(defn expt-balance [id] {(keyword (f/unparse (f/formatters :year-month-day) (c/to-date-time (t/today)))) [{:balance 1000}
                                                                       {:party id
                                                                        :counter-party nil
                                                                        :amount 1000}]
                         (keyword (f/unparse (f/formatters :year-month-day) (c/to-date-time (t/plus (t/today)
                                                                                                    (t/days 1)))))
                                                                                                         [{:balance 951.43}
                                                                                                          {:party id
                                                                                                           :counter-party "Uber"
                                                                                                           :amount -45.23}
                                                                                                          {:party id
                                                                                                           :counter-party "Amazon"
                                                                                                           :amount -3.34}]
                         (keyword (f/unparse (f/formatters :year-month-day) (c/to-date-time (t/plus (t/today)
                                                                                                    (t/days 2))))) [{:balance 771.43}
                                                                                        {:party id
                                                                                         :counter-party nil
                                                                                         :amount -180}]})

(deftest get-statement-test
  (def server (-main 5004))
  (let [id (create-scenario-account 5004)]
    (create-scenario-operations 5004 id)
    (let [response (client/get (str "http://localhost:5004/get-statement/" id))]
      (is (= (response :status) 200))
      (is (= (json/parse-string (response :body) true) (expt-balance id))))))

(deftest get-debt-periods-test
  (def server (-main 5005))
  (let [id (create-scenario-account 5005)]
    (create-scenario-operations 5005 id)
    (client/post "http://localhost:5005/operate"
                 {:body (json/generate-string {:party id
                                               :counter-party "TAM"
                                               :amount -800
                                               :offset 5})
                  :content-type :json})
    (client/post "http://localhost:5005/operate"
                 {:body (json/generate-string {:party id
                                               :counter-party nil
                                               :amount 100
                                               :offset 10})
                  :content-type :json})
    (let [response (client/get (str "http://localhost:5005/get-debt-periods/" id))]
      (is (= (response :status) 200))
      (is (= (json/parse-string (response :body) true) [{:principal 28.57
                                                         :start (f/unparse
                                                                  (f/formatters :year-month-day)
                                                                  (c/to-date-time
                                                                    (t/plus (t/today)
                                                                            (t/days 5))))
                                                         :end (f/unparse
                                                                (f/formatters :year-month-day)
                                                                (c/to-date-time
                                                                  (t/plus (t/today)
                                                                          (t/days 10))))}])))))


