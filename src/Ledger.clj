(ns ledger)

(def ^{:private true} *ledger* (ref '()))
(def ^{:private true} *accounts* (ref {}))

(defn new-account [name]
  (let  [new-id (inc (count @*accounts*))]
    (dosync
      (alter *accounts* conj [new-id {:name name
                                      :balance 0}]))))

(defn consolidate [& {:keys [party counter-party amount date]}]
  (dosync
    (alter *ledger* conj {:party party
                          :counter-party counter-party
                          :amount amount
                          :date date})
    (alter *accounts* [party :amount] + amount)))