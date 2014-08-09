clojure-paypal-ipn
==================

PayPal IPN handler for Clojure. Use with ring, compojure, or any clojure server env.

add this to your :dependencies in project.clj
```clojure
[clojure-paypal-ipn "0.0.1-SNAPSHOT"]
```

Use with ring / compojure
-------------------------

Add this to your ns requires
```clojure
  (:require [clojure-paypal-ipn.core :refer [make-ipn-handler]])
```
add this to your defroutes
```clojure
  (POST "/paypal/ipn" [] (make-ipn-handler
                           (fn [ipn-data]
                             ;ipn-data is a hash map of the paypal ipn data
                             ;Do your own processing here. Here are some things you aught to check:
                             ;  - payment_status is Completed
                             ;  - receiver_id/receiver_email is yours
                             ;  - txn_id has not been previously processed
                             ;  - payment_amount/payment_currency are correct
                             ;now write to the database or whatever
                             )
                           (fn [error]
                             ;error can be a:
                             ;  Throwable object - typically network issues
                             ;  Response hash map i.e. {:body "INVALID" ...}
                             ;  A message string
                             )))
```

Use with some other server setup
--------------------------------

Add this to your ns requires
```clojure
  (:require [clojure-paypal-ipn.core :refer [parse-paypal-ipn-string handle-ipn]])
```

Parse the raw contents string of the incoming HTTP POST (`body-str` in this sample)
```clojure
(let [ipn-data (clojure-paypal-ipn.core/parse-paypal-ipn-string body-str)]

  ;Promptly repsond with an HTTP/1.1 200 OK and close the connection

  ;Then do something like this
  (handle-ipn ipn-data
              (fn [ipn-data]
                ;ipn-data is a hash map of the paypal ipn data
                ;Do your own processing here. Here are some things you aught to check:
                ;  - payment_status is Completed
                ;  - receiver_id/receiver_email is yours
                ;  - txn_id has not been previously processed
                ;  - payment_amount/payment_currency are correct
                ;now write to the database or whatever
                )
              (fn [error]
                ;error can be a:
                ;  Throwable object - typically network issues
                ;  Response hash map i.e. {:body "INVALID" ...}
                ;  A message string
                )))
```

Contributing
------------

To run tests
```sh
$ lein quickie
```
