(ns nucredit.tests.ledger-tests
  (:require [clojure.test :refer :all]
            [nucredit.ledger :refer :all]
            [clj-time.core :as t]))

(deftest new-account-test
  (let [id (new-account "test")]
    (is (get-account id))))

(deftest account-status-event?-test
  (is (account-status-event? 100 -100))
  (is (account-status-event? -100 100))
  (is (account-status-event? -100 0))
  (is (account-status-event? 0 -100))
  (is (account-status-event? -100 -200))
  (is (not (account-status-event? 100 100)))
  (is (not (account-status-event? -100 -100)))
  (is (not (account-status-event? 100 0)))
  (is (not (account-status-event? 0 100)))
  (is (not (account-status-event? 0 0)))
  (is (not (account-status-event? 100 200))))

(def debt1-open {:principal 100M
                 :start (t/local-date 2017 8 17)
                 :end nil})

(def debt1 {:principal 100M
            :start (t/local-date 2017 8 17)
            :end (t/local-date 2017 8 18)})

(def debt2 {:principal 200M
            :start (t/local-date 2017 8 18)
            :end nil})

(deftest get-last-open-debt-test
  (intern 'nucredit.ledger 'debts (ref {1 [debt1 debt2]}))
  (is (= (get-last-open-debt 1) debt2))
  (intern 'nucredit.ledger 'debts (ref {}))
  (is (not (get-last-open-debt 1))))

(deftest finish-debt!-test
  (intern 'nucredit.ledger 'debts (ref {1 [debt1 debt2]}))
  (dosync
    (finish-debt! debt2 1 (t/local-date 2017 8 19)))
  (is (not (get-last-open-debt 1))))

(deftest add-new-debt!-test
  (intern 'nucredit.ledger 'debts (ref {}))
  (dosync
    (add-new-debt! 1 (t/local-date 2017 8 17) -100M))
  (is (= (get-last-open-debt 1) debt1-open)))

(def ledger1 '({:party 1 :counter-party nil :amount -180M :date (clj-time.core/local-date 2017 8 19)}
                {:party 1 :counter-party "Uber" :amount -45.23M :date (clj-time.core/local-date 2017 8 18)}
                {:party 1 :counter-party "Amazon" :amount -3.34M :date (clj-time.core/local-date 2017 8 18)}
                {:party 1 :counter-party nil :amount 1000M :date (clj-time.core/local-date 2017 8 17)}))

(def account1 {:name "NuBank" :balance 771.43M})

(def debts1 [])


(def ledger2 (conj (map eval ledger1) {:party 1 :counter-party "TAM" :amount -800M :date (t/local-date 2017 8 20)}
                   {:party 1 :counter-party nil :amount 100M :date (t/local-date 2017 8 25)}))

(def account2 {:name "NuBank" :balance 71.43M})

(def debts2 [{:principal 28.57M :start (t/local-date 2017 8 20) :end (t/local-date 2017 8 25)}])


(def ledger3 (conj ledger2 {:party 2 :counter-party nil :amount -1000M :date (t/local-date 2017 8 17)}
                   {:party 2 :counter-party nil :amount -1000M :date (t/local-date 2017 8 18)}))

(def account3 {:name "BTG" :balance -2000M})

(def debts3 [{:principal 1000M :start (t/local-date 2017 8 17) :end (t/local-date 2017 8 18)}
             {:principal 2000M :start (t/local-date 2017 8 18) :end nil}])

(deftest consolidate-test
  (intern 'nucredit.ledger 'ledger (ref '()))
  (intern 'nucredit.ledger 'accounts (ref {}))
  (intern 'nucredit.ledger 'debts (ref {}))
  (new-account "NuBank")
  (consolidate :party 1 :counter-party nil :amount 1000M :date (t/local-date 2017 8 17))
  (consolidate :party 1 :counter-party "Amazon" :amount -3.34M :date (t/local-date 2017 8 18))
  (consolidate :party 1 :counter-party "Uber" :amount -45.23M :date (t/local-date 2017 8 18))
  (consolidate :party 1 :counter-party nil :amount -180M :date (t/local-date 2017 8 19))
  (is (= @ledger (map eval ledger1)))
  (is (= (get-account 1) account1))
  (is (= (get-debts 1) debts1))
  (consolidate :party 1 :counter-party "TAM" :amount -800M :date (t/local-date 2017 8 20))
  (consolidate :party 1 :counter-party nil :amount 100M :date (t/local-date 2017 8 25))
  (is (= @ledger ledger2))
  (is (= (get-account 1) account2))
  (is (= (get-debts 1) debts2))
  (new-account "BTG")
  (consolidate :party 2 :counter-party nil :amount -1000M :date (t/local-date 2017 8 17))
  (consolidate :party 2 :counter-party nil :amount -1000M :date (t/local-date 2017 8 18))
  (is (= @ledger ledger3))
  (is (= (get-account 2) account3))
  (is (= (get-debts 2) debts3)))
