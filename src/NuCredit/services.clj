(ns nucredit.services
  (:require [nucredit.ledger :as ledger]
            [clj-time.core :as t]))

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