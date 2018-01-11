(ns bchain.coinmarketcap
  (:require [bchain.core :refer [http-call]]))


(defn btg->btc []
  (read-string (get (first (http-call "https://api.coinmarketcap.com/v1/ticker/" "bitcoin-gold")) "price_btc")))