(defproject clojure-paypal-ipn "0.0.1-SNAPSHOT"
  :description "PayPal IPN handler for Clojure. Use with ring, compojure, or any clojure server env."
  :url "https://github.com/smallhelm/clojure-paypal-ipn"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "0.9.2"]]
  :source-paths ["src"]

  :profiles {:dev {:plugins [[quickie "0.2.5"]]}})
