(ns clojure-paypal-ipn.core
  (:require [clojure.string :refer [join split trim]]
            [clj-http.client :as http-client]))

(defn parse-paypal-ipn-string [str]
  (if-not (string? str)
    {}
    (->> (split str #"\&")
         (map (fn [arg]
                (split arg #"\=")))
         (filter (fn [keyval]
                   (= 2 (count keyval))))
         (map (fn [[key val]]
                {key (java.net.URLDecoder/decode val)}))
         (apply merge {}))))

(def ipn-must-have-keys ["txn_id" "mc_currency" "mc_gross" "mc_fee" "receiver_id"])
(defn ipn-data-has-essentials? [ipn-data]
  (= 0 (count (filter (fn [key]
                        (not (contains? ipn-data key))) ipn-must-have-keys))))

(defn ask!-paypal [req-body sandbox?]
  (try
    (http-client/post (str "https://www." (if sandbox? "sandbox." "") "paypal.com/cgi-bin/webscr")
                      {:headers {"Connection"  "Close"
                                 "ContentType" "application/x-www-form-urlencoded"}
                       :body req-body
                       :socket-timeout (* 10 1000)
                       :conn-timeout   (* 10 1000)})
    (catch Throwable t
      t)))

(defn handle-ipn
  ([ipn-data on-success on-failure] (handle-ipn ipn-data on-success on-failure false))
  ([ipn-data on-success on-failure sandbox?]
   (if-not (ipn-data-has-essentials? ipn-data);don't even make an http call if it's bad ipn data
     (on-failure "Missing keys")

     (let [req-body (str "cmd=_notify-validate&"
                         (->> ipn-data
                              (map (fn [[key val]]
                                     (str key "=" (java.net.URLEncoder/encode val))))
                              (join "&")))
           response (ask!-paypal req-body sandbox?)]
       (if (and (map? response)
                (contains? response :body)
                (string? (:body response))
                (= "VERIFIED" (trim (:body response))))
         (on-success ipn-data)
         (on-failure response))))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; stuff for ring/compjure

(defn req->raw-body-str [req]
  (let [is (:body req)]
    (do
      (.reset is);TODO fix bug when sending empty post-data i.e. curl -i --data "a"  http://localhost:8080/paypal/ipn
      (let [raw-body-str (slurp is)]
        (do
          (.reset is)
          raw-body-str)))))

(defn make-ipn-handler
  ([on-success on-failure] (make-ipn-handler on-success on-failure false))
  ([on-success on-failure sandbox?]
   (fn [req]
     (let [body-str (req->raw-body-str req)
           ipn-data (parse-paypal-ipn-string body-str)]
       (do
         (.start (Thread. (fn [] (handle-ipn ipn-data on-success on-failure sandbox?))))
         ;respond to paypal right away, then go and process the ipn-data
         {:status  200
          :headers {"Content-Type" "text/html"}
          :body    ""})))))
