(ns ledger)

(def *ledger* (ref '()))
(def *accounts* (ref {}))

(defn new-account [name]
  (let  [newId (inc (count @*accounts*))]
    (dosync
      (alter *accounts* conj [new-id {:name name
                                      :balance 0}]))))

(defn consolidate [& {:keys [party counter-party amount date]}]
  (dosync
    (alter *ledger* conj {:party party
                          :counter-party counter-party
                          :amount amount
                          :date date})
    (alter *accounts* [pargity :amount] + amount)))