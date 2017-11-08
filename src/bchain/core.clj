(ns bchain.core
  "blockchain.info API"
  (:use [clojure.pprint])
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
     x#))


(defn- delim-str-of 
  ([d l]
  (reduce (fn [acc v]
          (if acc 
            (format "%s%s%s" acc d v)
            v)) l))
  ([l]
  (delim-str-of "|" l)))

(defn- reduce-args [args] 
  (reduce 
    (fn [acc [k v]]
      (format
        "%s%s=%s"
        (if acc 
          (format "%s&" acc)
          "") k v)) 
    nil 
    (partition 2 args)))

(def satoshi 0.00000001)

(defn- http-call [addr arg]
  (let [res (client/get (format "%s/%s" addr arg))]
    (if (= (:status res) 200) 
      (-> res :body json/read-str)
      (throw (IllegalStateException. (pr-str res))))))

(defn- blockchain-call [arg] (http-call "https://blockchain.info" arg))

(defn- blockchain-api-call [arg] (http-call "https://api.blockchain.info" arg))

(defn ticker [] (blockchain-call "ticker"))

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


;address lookups

(defmacro def-address-lookup [& args]
  `(do
     ~@(map 
         (fn [a] 
           `(defn ~a [confirmations# & addresses#] 
              (blockchain-query-call (format "%s/%s?confirmations=%s" ~(str a) (delim-str-of addresses#) confirmations#)))) args)))

(def-address-lookup 
  addressbalance 
  getreceivedbyaddress 
  getsentbyaddress
  )

(defn addressfirstseen [address] (blockchain-query-call (format "addressfirstseen/%s" address)))

(defn addressbalance 
  [confirmations & addresses]
   (blockchain-query-call (format "addressbalance/%s?confirmations=%s" (delim-str-of addresses) confirmations)))


;tx lookups

(defn txtotalbtcoutput [tx-hash] (blockchain-query-call (format "txtotalbtcoutput/%s" tx-hash)))
(defn txtotalbtcinput [tx-hash] (blockchain-query-call (format "txtotalbtcinput/%s" tx-hash)))
(defn txfee [tx-hash] (blockchain-query-call (format "txfee/%s" tx-hash)))
(defn txresult [tx-hash & addresses] (blockchain-query-call (format "txresult/%s/%s" tx-hash (delim-str-of addresses))))


;tools

(defn addresstohash [address] (blockchain-query-call (format "addresstohash/%s" address)))
(defn hashtoaddress [hash] (blockchain-query-call (format "hashtoaddress/%s" hash)))
(defn hashpubkey [pub-key] (blockchain-query-call (format "hashpubkey/%s" pub-key)))
(defn addrpubkey [pub-key] (blockchain-query-call (format "addrpubkey/%s" pub-key)))
(defn pubkeyaddr [address] (blockchain-query-call (format "pubkeyaddr/%s" address)))



;misc

(def-query 
  unconfirmedcount
  marketcap
  hashrate
  rejected)

(defn _24hrprice [] (blockchain-query-call "24hrprice"))
(defn _24hrtransactioncount  [] (blockchain-query-call "24hrtransactioncount "))
(defn _24hrbtcsent [] (blockchain-query-call "24hrbtcsent"))
(defn block-height [height] (blockchain-call (format "block-height/%s?format=json" height)))
(defn latestblock [] (blockchain-call "latestblock"))

(defn rawblock 
  ([block-hash] (blockchain-call (format "rawblock/%s" block-hash)))
  ([block-hash frmt] (blockchain-call (format "rawblock/%s?format=%s" block-hash frmt))))

(defn rawtx [tx] (blockchain-call (format "rawtx/%s" tx)))
(defn rawaddr [btc-addr] (blockchain-call (format "rawaddr/%s" btc-addr)))

(defn stats [] (blockchain-api-call "stats"))

 



(defn charts [type & args]
  (assert (-> args count even?))
  (blockchain-api-call (format "charts/%s" type)))

(defn pools 
  ([timespan] 
    (blockchain-api-call (format "pools?timespan=%s" timespan)))
  ([]
    (blockchain-api-call "pools")))

(comment 
  Below is not part of the blockchain.info API, it provides for example lazy seqs to some blockchain datastructures such as blocks and transactions.
)

(defn- rate-type-of [r t]
  `(defn ~(symbol (format "%s-%s" r t)) [] ((~r) ~t)))

(defn- rate-of [x]
  (let [k (key x)
        r (symbol k)]
    `(do 
       (defn ~r [] ((ticker) ~k))
       ~(rate-type-of r "last")
       ~(rate-type-of r "buy")
       ~(rate-type-of r "sell")
       ~(rate-type-of r "symbol")
       )))

(defmacro def-rates []
  "Define functions for all currencies, i.e (USD) for US-dollars"
  `(do
     ~@(map rate-of (ticker))))

(def-rates)

(defn rate-symbols- [] (into #{} (map symbol (keys (ticker)))))

(defn blocks 
  "all blocks in bitcoin as a lazy seq"
  ([] (blocks 0 (getblockcount)))
  ([i n] (if (< i n) 
           (lazy-seq (cons (block-height i) (blocks (inc i) n)))
           '())))
           
(defn- tx-of [blk] (mapcat #(% "tx") (blk "blocks")))

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

