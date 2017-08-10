(ns nucredit.tests.service-tests
  (:require [clojure.test :refer :all]
            [nucredit.services :refer :all]))

(deftest new-account-test
  (let [account (create-account "test")]
    (is account)
    (is (> (:account-number account) 0))
    (is (= (:name account) "test"))))