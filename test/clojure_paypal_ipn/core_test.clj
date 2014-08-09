(ns clojure-paypal-ipn.core-test
  (:require [clojure.test :refer :all]
            [clojure-paypal-ipn.core :refer :all]))

(deftest test-parse-paypal-ipn-string
  ;basic test
  (is (= {"hello" "world"} (parse-paypal-ipn-string "hello=world")))
  (is (= {"hello" "world"
          "good"  "morning"} (parse-paypal-ipn-string "hello=world&good=morning")))

  ;proper encoding
  (is (= {"sneaky" "a=b&b=c"
          "good"  "morning"} (parse-paypal-ipn-string "sneaky=a%3Db%26b%3Dc&good=morning")))

  ;last definition wins
  (is (= {"version" "3"} (parse-paypal-ipn-string "version=1&version=2&version=3")))
  (is (= {"version" "3"
          "name"    "bob"} (parse-paypal-ipn-string "version=1&name=jim&version=2&name=bob&version=3")))

  ;no arrays
  (is (= {"arr[]" "c"} (parse-paypal-ipn-string "arr[]=a&arr[]=b&arr[]=c")))

  ;handle garbage
  (is (= {} (parse-paypal-ipn-string "")))
  (is (= {} (parse-paypal-ipn-string nil)))
  (is (= {} (parse-paypal-ipn-string (fn [] "asdf"))))
  (is (= {} (parse-paypal-ipn-string 1234)))
  (is (= {} (parse-paypal-ipn-string {:hello "world"}))))

(deftest test-ipn-data-has-essentials?
  ;true if it has the required keys
  (is (true? (ipn-data-has-essentials? {"txn_id"      "something"
                                        "mc_currency" "something"
                                        "mc_gross"    "something"
                                        "mc_fee"      "something"
                                        "receiver_id" "something"})))
  ;haveing extras is ok
  (is (true? (ipn-data-has-essentials? {"txn_id"      "something"
                                        "mc_currency" "something"
                                        "mc_gross"    "something"
                                        "mc_fee"      "something"
                                        "receiver_id" "something"
                                        "not normal"  "is ok"})))
  ;false if missing something
  (is (false? (ipn-data-has-essentials? {"txn_id"      "something"
                                         "mc_gross"    "something"
                                         "mc_fee"      "something"
                                         "receiver_id" "something"})))


  (is (false? (ipn-data-has-essentials? {"txn_id"      "something"
                                         "mc_currency-not" "something"
                                         "mc_gross"    "something"
                                         "mc_fee"      "something"
                                         "receiver_id" "something"}))))


(defn mock-ask!-paypal [response]
  (fn [req sandbox?]
    response))

(deftest test-handle-ipn
  (let [valid-ipn-data   {"txn_id"      "something"
                          "mc_currency" "something"
                          "mc_gross"    "something"
                          "mc_fee"      "something"
                          "receiver_id" "something"}
        invalid-ipn-data {"txn_id"      "something"
                          "mc_currency" "something"
                          "mc_fee"      "something"
                          "receiver_id" "something"}]
    (do
      (with-redefs [ask!-paypal (mock-ask!-paypal {:body "VERIFIED"})]
        ;one that works
        (handle-ipn valid-ipn-data
                    (fn [ipn-data]
                      (is (= valid-ipn-data ipn-data)))
                    (fn [error]
                      (is false)))

        ;don't even make an http call if it's bad ipn data
        (handle-ipn invalid-ipn-data
                    (fn [ipn-data]
                      (is false))
                    (fn [error]
                      (is (= error "Missing keys")))))

      ;any other response fails
      (with-redefs [ask!-paypal (mock-ask!-paypal {:body "notVERIFIED"})]
        (handle-ipn valid-ipn-data
                    (fn [ipn-data]
                      (is false))
                    (fn [error]
                      (is (= error {:body "notVERIFIED"}))))
        (handle-ipn invalid-ipn-data
                    (fn [ipn-data]
                      (is false))
                    (fn [error]
                      (is (= error "Missing keys")))))

      (with-redefs [ask!-paypal (mock-ask!-paypal nil)]
        (handle-ipn valid-ipn-data
                    (fn [ipn-data]
                      (is false))
                    (fn [error]
                      (is (= error nil)))))

      (let [some-java-exception (Exception. "Something went wrong making the http call...")]
        (with-redefs [ask!-paypal (mock-ask!-paypal some-java-exception)]
          (handle-ipn valid-ipn-data
                      (fn [ipn-data]
                        (is false))
                      (fn [error]
                        (is (= error some-java-exception)))))))))
