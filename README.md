clojure-paypal-ipn
==================

PayPal IPN handler for Clojure. Use with ring, compojure, or any clojure server env.

Add this to your `:dependencies` in project.clj
```clojure
[clojure-paypal-ipn "0.0.1-SNAPSHOT"]
```

Use with ring / compojure
-------------------------
```clojure
;Add this to your ns requires
  (:require [clojure-paypal-ipn.core :refer [make-ipn-handler]])

...

;Add this to your defroutes
  (POST "/paypal/ipn" [] (make-ipn-handler
                           (fn [ipn-data]
                             ;ipn-data is a hash map of the paypal ipn data
                             ;Here are some things you aught to check:
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
(let [ipn-data (parse-paypal-ipn-string body-str)]

  ;Promptly repsond with an HTTP/1.1 200 OK and close the connection

  ;Then do something like this
  (handle-ipn ipn-data
              (fn [ipn-data]
                ;ipn-data is a hash map of the paypal ipn data
                ;Here are some things you aught to check:
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

License
-------
The MIT License (MIT)

Copyright (c) 2014 Small Helm LLC

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
