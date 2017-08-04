(ns services
  (:require [ledger]))

(defn create-account [name]
  (let [account (ledger/new-account name)]
    {:account-number (first (keys account))
     :name name}))