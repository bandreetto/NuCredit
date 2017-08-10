(ns nucredit.services
  (:require [nucredit.ledger :as ledger]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clojure.set :as set]))

(defn remove-date [m]
  (dissoc m :date))

(defn parse-date-keys [m]
  (zipmap (map (comp
                 #(f/unparse (f/formatters :year-month-day) %)
                 #(c/to-date-time %))
               (keys m))
          (map #(map remove-date %) (vals m))))

(defn get-balance [account-id]
  (if-let [account (ledger/get-account account-id)]
    account
    {:error (str "No accounts with id: " account-id " found")}))

(defn get-statement [account-id]
  (if-let [account (ledger/get-account account-id)]
    (parse-date-keys (sort (group-by :date (ledger/get-statements account-id))))
    {:error (str "No accounts with id: " account-id " found")}))

(defn create-account [name]
  (let [account-id (ledger/new-account name)]
    {:account-number account-id
     :name name}))

(defn operate [party counter-party amount offset]
  (if-let [account (ledger/get-account party)]
    (ledger/consolidate :party party
                        :counter-party counter-party
                        :amount amount
                        :date (t/plus
                                (t/today)
                                (t/days (read-string (or offset
                                                         "0")))))
    {:error (str "No accounts with id: " party " found")}))

