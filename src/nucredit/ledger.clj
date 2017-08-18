(ns nucredit.ledger
  (:require [clj-time.core :as t]))

(def ledger (ref '()))
(def accounts (ref {}))
(def debts (ref {}))

(defn new-account [name]
  (let  [new-id (inc (count @accounts))]
    (dosync
      (alter accounts conj [new-id {:name    name
                                    :balance 0}])
      (alter debts conj [new-id []])
      new-id)))

(defn account-status-event? [curr, next]
  ; Check if next has become negative or if it returned from negative to positive.
  (if (< next 0)
    true
    (if (< curr 0)
      true
      false)))

(defn get-last-open-debt [account-id]
  (if-let [debts (@debts account-id)]
    (first (filter #(nil? (% :end)) debts))
    nil))

(defn finish-debt! [last-debt account-id date]
  (if (t/before? (last-debt :start) date)
    (alter debts update account-id assoc
           (.indexOf (@debts account-id) last-debt)
           (assoc last-debt :end date))
    (alter debts update account-id remove #(= last-debt %))))

(defn add-new-debt!
  ([account-id date amount]
   (alter debts update account-id conj {:principal (- amount)
                                        :start date
                                        :end nil}))
  ([last-debt account-id date amount]
   (finish-debt! last-debt account-id date)
   (alter debts update account-id conj {:principal (- amount)
                                        :start date
                                        :end nil})))

(defn consolidate [& {:keys [party counter-party amount date]}]
  (dosync
    (alter ledger conj {:party           party
                        :counter-party   counter-party
                        :amount          amount
                        :date            date})

    ; Check and fire debt status event
    (let [current (get-in @accounts [party :balance])
          next (+ current amount)]
      (if (account-status-event? current next)
        (if (< next 0)
          (if-let [last-debt (get-last-open-debt party)]
            (add-new-debt! last-debt party date next)
            (add-new-debt! party date next))
          (finish-debt! (get-last-open-debt party) party date))))

    (alter accounts update-in [party :balance] + amount)))

(defn get-account [account-id]
  (@accounts account-id))

(defn get-statements [account-id]
  (filter #(= (:party %) account-id) @ledger))

(defn get-debts [account-id]
  (@debts account-id))