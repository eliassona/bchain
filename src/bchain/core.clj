(ns bchain.core
  "Functions using the blockchain.info API"
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
     x#))

(defn blockchain-call [arg]
  (let [res (client/get (format "https://blockchain.info/%s" arg))]
    (if (= (:status res) 200) 
      (-> res :body json/read-str)
      (throw (IllegalStateException. (pr-str res))))))


(defn exchange-rates [] (blockchain-call "ticker"))

(defn dollar-rate [] (((exchange-rates) "USD") "last"))

(defn tobtc [value to-currency]
  (blockchain-call (format "tobtc?currency=%s&value=%s" to-currency value)))

(defn blockchain-query-call [arg] (blockchain-call (format "q/%s" arg)))

(defmacro def-query [& args]
  `(do 
     ~@(map (fn [a] `(defn ~a [] (blockchain-query-call ~(str a)))) args)))


;realtime
(def-query 
  getdifficulty 
  getblockcount 
  latesthash
  bcperblock
  totalbc
  probability
  hashestowin
  nextretarget
  avgtxsize 
  avgtxvalue
  interval 
  eta
  avgtxnumber)


;misc

(def-query 
  unconfirmedcount
  marketcap
  hashrate
  rejected)
  
  


(defn _24hrprice [] (blockchain-query-call "24hrprice"))
(defn _24hrtransactioncount  [] (blockchain-query-call "24hrtransactioncount "))
(defn _24hrbtcsent [] (blockchain-query-call "24hrbtcsent"))


(defn block-of [height] (blockchain-call (format "block-height/%s?format=json" height)))

(defn latestblock [] (blockchain-call "latestblock"))

(defn rawblock 
  ([block-hash] (blockchain-call (format "rawblock/%s" block-hash)))
  ([block-hash frmt] (blockchain-call (format "rawblock/%s?format=%s" block-hash frmt))))

(defn rawtx [tx] (blockchain-call (format "rawtx/%s" tx)))
(defn rawtaddr [btc-addr] (blockchain-call (format "rawtaddr/%s" btc-addr)))

(defn blocks 
  "all blocks in bitcoin as a lazy seq"
  ([] (blocks 0 (getblockcount) ))
  ([i n] (if (< i n) 
           (lazy-seq (cons (block-of i) (blocks (inc i) n)))
           '())))
           
(defn tx-of [blk] (mapcat #(% "tx") (blk "blocks")))

(defn transactions 
  "all transactions in bitcoin as a lazy seq"
  ([] (transactions (blocks)))
  ([blks]
    (if 
      (empty? blks)
      []
      (lazy-seq 
        (concat 
          (tx-of (first blks)) (transactions (rest blks)))))))
  
  

