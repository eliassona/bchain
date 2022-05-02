(ns bchain.umbrel
  (:use [clojure.pprint])
  (:require [clojure.data.json :as json]
            [clojure.repl :refer [source doc]])
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
     ~@(map (partial arity-of password cmd conv-fn) (args-of args))))

(defmacro def-api 
  ([password cmd conv-fn args]
    (defn-of password cmd conv-fn args))
  ([password cmd conv-fn]
    `(def-api ~password ~cmd ~conv-fn []))
  ([password cmd]
    `(def-api ~password ~cmd identity))
  )
  
(defn create-api [password]
;   == Blockchain ==   
	(def-api password getbestblockhash .trim)
	(def-api password getblock json/read-str [blockhash [verbosity]])
	(def-api password getblockchaininfo json/read-str)
	(def-api password getblockcount json/read-str)
	(def-api password getblockfilter identity [blockhash [verbosity]])
	(def-api password getblockhash .trim [index])
	(def-api password getblockheader json/read-str [blockhash [verbosity]])
	(def-api password getblockstats identity [hash_or_height [stats]])
	(def-api password getchaintips json/read-str)
  (def-api password getchaintxstats json/read-str [[nblocks blockhash]])
  (def-api password getdifficulty json/read-str)
  (def-api password getmempoolancestors identity [txid [verbose]])
  (def-api password getmempooldescendants identity [txid [verbose]])
  (def-api password getmempoolentry identity [txid])
  (def-api password getmempoolinfo json/read-str)
  (def-api password getrawmempool json/read-str [[verbose mempool_sequence]])
  (def-api password gettxout identity [txid n [include_mempool]])
  (def-api password gettxoutproof identity [txids [blockhash]]);["txid",...] ( "blockhash" )
  (def-api password gettxoutsetinfo json/read-str [[hash_type hash_or_height use_index]])
  (def-api password preciousblock identity [blockhash])
  (def-api password pruneblockchain identity [height])
  (def-api password savemempool)
  (def-api password scantxoutset identity [action [scanobjects]])
  (def-api password verifychain identity [checklevel nblocks])
  (def-api password verifytxoutproof identity [proof])
  
;  == Control == 
  (def-api password getmemoryinfo json/read-str [[mode]])
  (def-api password getrpcinfo json/read-str)
  (def-api password help identity [[command]])
  (def-api password logging json/read-str [[include_category exclude_category]])
  (def-api password stop)
  (def-api password uptime json/read-str)
  
;  == Generating ==
  (def-api password generateblock identity [output rawtx-txid])
  (def-api password generatetoaddress identity [nblocks address [maxtries]])
  (def-api password generatetodescriptor identity [num_blocks descriptor [maxtries]])

;  == Mining ==
  (def-api password getblocktemplate identity [[template_request]])
  (def-api password getmininginfo json/read-str) 
  (def-api password getnetworkhashps json/read-str [[nblocks height]])
  (def-api password prioritisetransaction identity [txid dummy fee_delta])
  (def-api password submitblock identity [hexdata [dummy]])
  (def-api password submitheader identity [hexdata])

;  == Network ==
  (def-api password addnode identity [node command])
  (def-api password clearbanned identity) 
  (def-api password disconnectnode identity [[address nodeid]])
  (def-api password getaddednodeinfo json/read-str [[node]])
  (def-api password getconnectioncount json/read-str) 
  (def-api password getnettotals json/read-str) 
  (def-api password getnetworkinfo json/read-str) 
  (def-api password getnodeaddresses json/read-str [[count network]])
  (def-api password getpeerinfo json/read-str) 
  (def-api password listbanned json/read-str) 
  (def-api password ping identity) 
  (def-api password setban identity [subnet command [bantime absolute]])
  (def-api password setnetworkactive identity [state])

;  == Rawtransactions ==
  (def-api password analyzepsbt identity [psbt])
  (def-api password combinepsbt identity [psbts])
  (def-api password combinerawtransaction identity [hexstrings])
  (def-api password converttopsbt identity [hexstring [permitsigdata iswitness]])
  (def-api password createpsbt identity) ;[{txid:hex,vout:n,sequence:n},...] [{address:amount,...},{data:hex},...] ( locktime replaceable )
  (def-api password createrawtransaction identity); [{txid:hex,vout:n,sequence:n},...] [{address:amount,...},{data:hex},...] ( locktime replaceable )
  (def-api password decodepsbt identity [psbt])
  (def-api password decoderawtransaction identity [hexstring [iswitness]])
  (def-api password decodescript identity [hexstring])
  (def-api password finalizepsbt identity [psbt [extract]])
  (def-api password fundrawtransaction identity [hexstring [options iswitness]])
  (def-api password getrawtransaction identity [txid [verbose blockhash]])
  (def-api password joinpsbts identity [psbts])
  (def-api password sendrawtransaction identity [hexstring [maxfeerate]])
  (def-api password signrawtransactionwithkey identity [hexstring privatekeys]) ;[( [{txid:hex,vout:n,scriptPubKey:hex,redeemScript:hex,witnessScript:hex,amount:amount},...] sighashtype )
  (def-api password testmempoolaccept identity [rawtxs [maxfeerate]])
  (def-api password utxoupdatepsbt identity [psbt]) ; ( [,{desc:str,range:n or [n,n]},...] )

;  == Signer ==
  (def-api password enumeratesigners identity) 

;  == Util ==
  (def-api password createmultisig identity [nrequired keys [address_type]])
  (def-api password deriveaddresses identity [descriptor [range]])
  (def-api password estimatesmartfee identity [conf_target [estimate_mode]])
  (def-api password getdescriptorinfo identity [descriptor])
  (def-api password getindexinfo identity [[index_name]])
  (def-api password signmessagewithprivkey identity [privkey message])
  (def-api password validateaddress identity [address])
  (def-api password verifymessage identity [address signature message])

;  == Wallet ==
  (def-api password abandontransaction identity [txid])
  (def-api password abortrescan identity) 
  (def-api password addmultisigaddress identity [nrequired keys [label address_type]])
  (def-api password backupwallet identity [destination])
  (def-api password bumpfee identity [txid [options]])
  (def-api password createwallet identity [wallet_name [disable_private_keys blank passphrase avoid_reuse descriptors load_on_startup external_signe]])
  (def-api password dumpprivkey identity [address])
  (def-api password dumpwallet identity [filename])
  (def-api password encryptwallet identity [passphrase])
  (def-api password getaddressesbylabel identity [label])
  (def-api password getaddressinfo identity [address])
  (def-api password getbalance identity [[dummy minconf include_watchonly avoid_reuse]])
  (def-api password getbalances identity) 
  (def-api password getnewaddress identity [[label address_type]])
  (def-api password getrawchangeaddress identity [[address_type]])
  (def-api password getreceivedbyaddress identity [address [minconf]])
  (def-api password getreceivedbylabel identity [label [minconf]])
  (def-api password gettransaction identity [txid [include_watchonly verbose]])
  (def-api password getunconfirmedbalance identity) 
  (def-api password getwalletinfo identity) 
  (def-api password importaddress identity [address [label rescan p2sh]])
  (def-api password importdescriptors identity [requests])
  (def-api password importmulti identity [requests [options]])
  (def-api password importprivkey identity [privkey [label rescan]])
  (def-api password importprunedfunds identity [rawtransaction txoutproof])
  (def-api password importpubkey identity [pubkey [label rescan]])
  (def-api password importwallet identity [filename])
  (def-api password keypoolrefill identity [[newsize]])
  (def-api password listaddressgroupings identity) 
  (def-api password listdescriptors identity) 
  (def-api password listlabels identity [[purpose]])
  (def-api password listlockunspent identity) 
  (def-api password listreceivedbyaddress identity [[minconf include_empty include_watchonly address_filter]])
  (def-api password listreceivedbylabel identity [[minconf include_empty include_watchonly]])
  (def-api password listsinceblock identity [[blockhash target_confirmations include_watchonly include_removed]])
  (def-api password listtransactions identity [[label count skip include_watchonly]])
  (def-api password listunspent identity [[minconf maxconf addresses include_unsafe query_options]])
  (def-api password listwalletdir json/read-str) 
  (def-api password listwallets json/read-str) 
  (def-api password loadwallet identity [filename [load_on_startup]])
  (def-api password lockunspent identity [unlock]) ;( [{txid:hex,vout:n},...] )
  (def-api password psbtbumpfee identity [txid [ options]])
  (def-api password removeprunedfunds identity [txid])
  (def-api password rescanblockchain identity [[start_height stop_height]])
  (def-api password _send identity)  ;[{address:amount,...},{data:hex},...] ( conf_target estimate_mode fee_rate options )
  (def-api password sendmany identity) ;  {address:amount,...} ( minconf comment [address,...] replaceable conf_target estimate_mode fee_rate verbose )
  (def-api password sendtoaddress identity [address amount [comment comment_to subtractfeefromamount replaceable conf_target estimate_mode avoid_reuse fee_rate verbose]])
  (def-api password sethdseed identity [[newkeypool seed]])
  (def-api password setlabel identity [address label])
  (def-api password settxfee identity [amount])
  (def-api password setwalletflag identity [flag [value]])
  (def-api password signmessage identity [address message])
  (def-api password signrawtransactionwithwallet identity [hexstring]) ;( [{txid:hex,vout:n,scriptPubKey:hex,redeemScript:hex,witnessScript:hex,amount:amount},...] sighashtype )
  (def-api password unloadwallet identity [[wallet_name load_on_startup]])
  (def-api password upgradewallet identity [[version]])
  (def-api password walletcreatefundedpsbt identity)  ;( [{txid:hex,vout:n,sequence:n},...] ) [{address:amount,...},{data:hex},...] ( locktime options bip32derivs )
  (def-api password walletdisplayaddress identity [bitcoin address to display])
  (def-api password walletlock identity) 
  (def-api password walletpassphrase identity [passphrase timeout])
  (def-api password walletpassphrasechange identity [oldpassphrase newpassphrase])
  (def-api password walletprocesspsbt identity [psbt [sign sighashtype bip32derivs]])

;  == Zmq ==
  (def-api password getzmqnotifications json/read-str)
  
)

