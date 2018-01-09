(ns bchain.rate
  (:require [bchain.core :refer [exchange-rates dbg]]))


(defn rate-proc [currency]
  (loop [l []]
    (let [v ((exchange-rates) currency)]
      (Thread/sleep 1000)
      (if (>= (count l) 60)
        (recur (conj (rest l) v)) 
        (recur (conj l v))))))