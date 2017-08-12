(ns nucredit.ledger)

(def ledger (ref '()))
(def accounts (ref {}))

(defn new-account [name]
  (let  [new-id (inc (count @accounts))]
    (dosync
      (alter accounts conj [new-id {:name    name
                                    :balance 0}])
      new-id)))

(defn consolidate [& {:keys [party counter-party amount date]}]
  (dosync
    (alter ledger conj {:party           party
                        :counter-party   counter-party
                        :amount          amount
                        :date            date})
    (alter accounts update-in [party :balance] + amount)))

(defn get-account [account-id]
  (@accounts account-id))

(defn get-statements [account-id]
  (filter #(= (:party %) account-id) @ledger))