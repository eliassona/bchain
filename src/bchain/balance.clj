(ns bchain.balance
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

(defmethod convert :ss [from to]
  (update (ss/rate from to) "rate" read-string))

(defn pair-it [value from to]
  {"pair" (format "%s_%s" from to) "rate" value})

(defmacro def-convert [from to expr]
  `(do 
     (defmethod convert [~from ~to] [from# to#]
       (pair-it ~expr from# to#))
     (defmethod convert [~to ~from] [to# from#]
       (pair-it (/ 1 ~expr) from# to#))))

(def-convert "SEK" "USD" (/ (USD-last) (SEK-last)))
(def-convert "BTC" "USD" (USD-last))
(def-convert "BTC" "SEK" (SEK-last))


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

