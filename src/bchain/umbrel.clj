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
    

(defn format-str-of [n]
  (reduce 
    (fn [acc _] 
      (if acc 
        (str acc " %s") 
        (str "%s"))) 
    nil (range n)))

(defn remove-last-if-optional [args]
  (if (vector? (last args))
    (vec (butlast args))
    args))


(defn args-of [args]
  (if-let [opt (last args)]
    (if (vector? opt)
      (loop [res [(vec (butlast args))]
             opt opt]
        (if (empty? opt)
          res
          (recur (conj res (conj (last res) (first opt))) (rest opt))))
     [args])
    [args]))

(defn arity-of [password cmd conv-fn args]
  `(~args
     (~conv-fn 
       (bitcoin-cli ~password (format ~(format-str-of (inc (count args))) ~(str cmd) ~@args)))))

(defn defn-of [password cmd conv-fn args]
  `(defn 
     ~cmd
     ~@(map (fn [a] (arity-of password cmd conv-fn a)) (args-of args))))

(defmacro def-api 
  ([password cmd conv-fn args]
    (defn-of password cmd conv-fn args))
  ([password cmd conv-fn]
    `(def-api ~password ~cmd ~conv-fn []))
  ([password cmd]
    `(def-api ~password ~cmd identity))
  )
  
(defn create-api [password]
	(def-api password getbestblockhash .trim)
	(def-api password getblock identity [blockhash [verbosity]])
	(def-api password getblockchaininfo json/read-str)
	(def-api password getblockcount read-string)
	(def-api password getblockfilter identity [blockhash [verbosity]])
	(def-api password getblockhash .trim [index])
	(def-api password getblockheader identity [blockhash [verbosity]])
	(def-api password getblockstats identity [hash_or_height [stats]])
	(def-api password getchaintips json/read-str)
 )

