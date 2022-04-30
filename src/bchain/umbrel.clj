(ns bchain.umbrel
  (:use [clojure.pprint])
  (:require [clojure.data.json :as json])
  (:import [com.jcraft.jsch JSch]
           [java.io ByteArrayOutputStream]))

(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
     x#))

(defn ssh [user host port password command]
  (let [session (.getSession (JSch.) user host port)
        out (ByteArrayOutputStream.)]
    (try 
	    (.setPassword session password)
	    (.setConfig session "StrictHostKeyChecking" "no")
	    (.connect session)
	    (let [channel (.openChannel session "exec")]
        (try 
		      (.setCommand channel command)
		      (.setOutputStream channel out)
		      (.connect channel)
		      (while (.isConnected channel)
		        (Thread/sleep 100))
		      (String. (.toByteArray out))
          (finally 
            (.disconnect channel))))
     (finally
       (.disconnect session)))))

(def umbrel (partial ssh "umbrel" "umbrel.local" 22))

(defn bitcoin-cli [password cmd]
  (umbrel password (format "cd ~/umbrel/bin; docker exec bitcoin bitcoin-cli %s" cmd)))
    
#_(def bitcoin-cli (partial bitcoin-cli "mypw"))    

(defn format-str-of [n]
  (reduce 
    (fn [acc _] 
      (if acc 
        (str acc " %s") 
        (str "%s"))) 
    nil (range n)))

(defmacro def-api 
  ([cmd conv-fn args]
    (let [pw (symbol "password")]
      `(defn ~cmd 
         (~args (~conv-fn (bitcoin-cli (format ~(format-str-of (inc (count args))) ~(str cmd) ~@args))))
         (~(vec (concat [pw] args)) (~conv-fn (bitcoin-cli ~pw (format ~(format-str-of (inc (count args))) ~(str cmd) ~@args)))))))
  ([cmd conv-fn]
    `(def-api ~cmd ~conv-fn []))
  ([cmd]
    `(def-api ~cmd identity))
  )
  
(def-api getbestblockhash identity)
(def-api getblock identity [blockhash verbosity])
(def-api getblockchaininfo json/read-str)
(def-api getblockcount read-string)
(def-api getblockfilter identity [blockhash verbosity])
(def-api getblockhash identity [index])
(def-api getblockheader identity [blockhash verbosity])
(def-api getblockstats identity [hash_or_height stats])
(def-api getchaintips)

