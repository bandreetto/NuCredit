(ns nucredit.services
  (:require [nucredit.ledger :as ledger]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [clojure.set :as set]))

; Get Statement

(defn remove-date [operation]
  (dissoc operation :date))

(defn sum-operations
  [base operations]
   (apply + (conj (map :amount operations) base)))

(defn create-balance-map [balance]
  (zipmap [:balance] [balance]))

(defn add-balance [operations-vector]
  (map conj
       operations-vector
       (map create-balance-map (drop 1 (reductions sum-operations 0 operations-vector)))))

(defn format-statement [statement]
  ; Formats the statement organizing it by dates and add the total balance of each date
  (zipmap (map (comp
                 #(f/unparse (f/formatters :year-month-day) %)
                 #(c/to-date-time %))
               (keys statement))
          (add-balance (map #(map remove-date %) (vals statement)))))

(defn get-statement [account-id]
  (if-let [account (ledger/get-account account-id)]
    (format-statement (sort (group-by :date (ledger/get-statements account-id))))
    {:error (str "No accounts with id: " account-id " found")}))

; Get Balance

(defn get-balance [account-id]
  (if-let [account (ledger/get-account account-id)]
    account
    {:error (str "No accounts with id: " account-id " found")}))

; Create Account

(defn create-account [name]
  (let [account-id (ledger/new-account name)]
    {:account-number account-id
     :name name}))

; Operate

(defn operate [party counter-party amount offset]
  (if-let [account (ledger/get-account party)]
    ((ledger/consolidate :party party
                        :counter-party counter-party
                        :amount amount
                        :date (t/plus
                                (t/today)
                                (t/days (read-string (or offset
                                                         "0"))))) party)
    {:error (str "No accounts with id: " party " found")}))

