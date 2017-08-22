(ns nucredit.tests.service-tests
  (:require [clojure.test :refer :all]
            [nucredit.services :refer :all]
            [clj-time.core :as t]))

; Create Account

(deftest new-account-test
  (let [account (create-account "test")]
    (is account)
    (is (> (:account-number account) 0))
    (is (= (:name account) "test"))))

; Get Debt Periods

(def open-debt {:principal 100 :start (t/local-date 2017 8 17) :end nil})
(def closed-debt {:principal 100 :start (t/local-date 2017 8 17) :end (t/local-date 2017 8 18)})

(deftest format-end-date-open-debt
  (let [debt (format-end-date open-debt)]
    (is (= debt open-debt))))

(deftest format-end-date-closed-debt
  (let [debt (format-end-date closed-debt)]
    (is (= (debt :end) "2017-08-18"))))

(deftest format-start-date-test
  (let [debt (format-start-date open-debt)]
    (is (= (debt :start) "2017-08-17"))))

(deftest format-debt-dates-test
  (let [debts (format-debt-dates [open-debt closed-debt])]
    (is (every? #(= (% :start) "2017-08-17") debts))
    (is (every? #(or (= (% :end) "2017-08-18") (= (% :end) nil)) debts))))

; Get Statement

(def operation1 {:party 1 :counter-party "Uber" :amount 1 :date (t/local-date 2017 8 17)})
(def operation2 {:party 1 :counter-party "Amazon" :amount 2 :date (t/local-date 2017 8 17)})
(def operation3 {:party 1 :counter-party nil :amount 3 :date (t/local-date 2017 8 18)})
(def operation4 {:party 1 :counter-party nil :amount -1 :date (t/local-date 2017 8 18)})

(def test-statement {(t/local-date 2017 8 17) [operation1 operation2]
                (t/local-date 2017 8 18) [operation3 operation4]})


(deftest remove-date-test
  (let [operation (remove-date operation1)]
    (is (not (operation :date)))))

(deftest sum-operations-test
  (let [sum (sum-operations 0 [operation1 operation2 operation3 operation4])]
    (is (= sum 5))))

(deftest create-balance-map-test
  (let [b-map (create-balance-map "test")]
    (is (= b-map {:balance "test"}))))

(deftest add-balance-test
  (let [operations-vector (add-balance [[operation1 operation2] [operation3 operation4]])]
    (is (some #{{:balance 3}} (first operations-vector)))
    (is (some #{{:balance 5}} (second operations-vector)))))

(deftest format-statement-test
  (let [statement (format-statement test-statement)
        firstStatement (statement "2017-08-17")
        secondStatement (statement "2017-08-18")]
    (is (some #{{:balance 3}} firstStatement))
    (is (some #{(dissoc operation1 :date)} firstStatement))
    (is (some #{(dissoc operation2 :date)} firstStatement))
    (is (some #{{:balance 5}} secondStatement))
    (is (some #{(dissoc operation3 :date)} secondStatement))
    (is (some #{(dissoc operation4 :date)} secondStatement))))
