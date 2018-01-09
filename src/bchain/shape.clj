(ns bchain.shape
  (:require [bchain.core :refer [http-call]])
  )



(defn- shapeshift-call [arg] (http-call "https://shapeshift.io" arg))

(defn pair-call [call from to] (shapeshift-call (format "%s/%s_%s" call from to)))

(defn getcoins [] (shapeshift-call "getcoins"))

(defn- available? [e] (= (-> e val (get "status")) "available"))
(defn available-coins [] (filter available? (getcoins)))

(defn coin-set-of [f] (->> (f) keys (into #{})))
(defn coin-set [] (coin-set-of getcoins))
(defn available-coin-set [] (coin-set-of available-coins))

(defn rate [from to] (pair-call "rate" from to))
(defn limit [from to] (pair-call "limit" from to))
(defn marketinfo [from to] (pair-call "marketinfo" from to))


(defn recenttx 
  ([] (shapeshift-call "recenttx"))
  ([max] (shapeshift-call (format "recenttx/%s" max))))

(defn txStat [address] (shapeshift-call (format "txStat/%s" address)))

