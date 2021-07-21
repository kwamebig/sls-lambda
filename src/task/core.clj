(ns task.core
  (:require [clojure.data.json :as json]
            [clojure.java.io   :as io]
            [clojure.string :as str])
  (:gen-class
   :name       task.core.ApiHandler
   :implements [com.amazonaws.services.lambda.runtime.RequestStreamHandler]))

;; BASIC CALCULATOR
;; Addition, subtraction, multiplying, and division

(defmulti calculator (fn [operation] (:operation operation)))

(defmethod calculator "+" [x] 
  (+ (:a x) (:b x)))
(defmethod calculator "-" [x] 
  (- (:a x) (:b x)))
(defmethod calculator "*" [x] 
  (* (:a x) (:b x)))
(defmethod calculator "/" [x] 
  (if (= 0.0 (:b x)) "Error! Division by zero is not possible" (/ (:a x) (:b x))))

;; HTTP HANDLER

(defn handle-request [handler]
  (fn [_ input-stream output-stream context]
    (with-open [in  (io/reader input-stream)
                out (io/writer output-stream)]
      (let [request (json/read in :key-fn keyword)]
        (-> request
            (handler context)
            (json/write out))))))
          

(def third (fn [coll] (get coll 2)))

(def -handleRequest
  (handle-request
   (fn [event context]
     (prn event)
     (let [expr (json/read-str (:body event) :key-fn keyword)
           splitted-expr (vec (str/split (:expr expr) #" "))
           a (Double. (str (first splitted-expr)))
           operation (str (third splitted-expr))
           b (Double. (str (last splitted-expr)))
           result (calculator (assoc {} :a a :operation operation :b b))]
     {:status 200
      :body   result}))))