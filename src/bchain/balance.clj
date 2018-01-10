(ns bchain.balance
  (:use [clojure.pprint]
        [clojure.set])
  (:require [bchain.shape :as ss]
            [bchain.core :refer [dbg rawaddr SEK SEK-last USD USD-last satoshi blocks]])
  )

(defmulti balance identity)

(defmulti convert 
  (fn [from to] 
    (let [c (partial contains? (ss/coin-set))]
      (if (and (c from) (c to))
        :ss
        [from to]))))

(def conversion-pair (atom {}))

(defn update-pair! [from to]
 (swap! conversion-pair (fn [v] (update v from #(if % (conj % to) #{to}))))) 

(defn add-ss-rates! []
  (let [sc (ss/coin-set)
        all-comb (for [x sc
                       y sc
                       :when (not= x y)]
        [x y])]
    (doseq [[x y] all-comb]
      (update-pair! x y))))

(add-ss-rates!)

(defmethod convert :ss [from to]
  (update (ss/rate from to) "rate" read-string))

(defn pair-it [value from to]
  {"pair" (format "%s_%s" from to) "rate" value})

(defn do-convert [from to expr]
  `(do
     (update-pair! ~from ~to)
     (defmethod convert [~from ~to] [from# to#]
          (pair-it ~expr from# to#))))

(defmacro def-convert [from to expr]
  `(do
     ~(do-convert from to expr)
     ~(do-convert to from `(/ 1 ~expr))))

(defn rate-of [m] (m "rate"))

(defmethod convert :default [from to]
  (let [cp @conversion-pair
        btw (first (intersection (cp from) (cp to)))]
    (if btw
    (do 
      (eval (def-convert from to (* (rate-of (convert from btw)) (rate-of (convert btw to)))))
      (convert from to))
    (throw (IllegalStateException. (format "Can't convert %s to %s" from to))))
    ))

(def-convert "BTC" "SEK" (SEK-last))
(def-convert "BTC" "USD" (USD-last))


(def units (atom #{}))

(defmacro def-balance [unit expr]
  `(do 
     (defmethod balance ~unit [unit#] {:value ~expr, :unit ~unit})
     (when-not (= ~unit :default) 
       (swap! units conj ~unit))))

(defn could-not-convert [b]
  (when (> (:value b) 0)
    (println (format "Could not convert for %s" (:unit b))))
  0
  )

(defn convert-to [from-balance to]
  (let [from (:unit from-balance)]
    (if (= from to) 
      from-balance
      (assoc from-balance :value 
       (if-let [rate ((convert from to) "rate")]
         (* (:value from-balance) rate)
         (could-not-convert from-balance)), :unit to))))

(defn total-balance [to]
  {:value (reduce (fn [acc v] (+ (:value v) acc)) 0 (map #(convert-to (balance %) to) @units)) :unit to})

