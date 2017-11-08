# bchain

A Clojure wrapper API for blockchain.info

## Usage
Add the following line to your leinigen dependencies:
```clojure
[bchain "0.1.0-SNAPSHOT"]
```

```clojure
=> (use 'bchain.core)
nil
```

The clojure functions in this API have the same name as the blockchain.info API as described here: https://blockchain.info/api

For example to see the genesis block:
```clojure
=> (block-height 0)
{"blocks" [{"height" 0, "mrkl_root" "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", "hash" "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f", "main_chain" true, "n_tx" 1, "block_index" 14849, "bits" 486604799, "fee" 0, "time" 1231006505, "tx" [{"vout_sz" 1, "relayed_by" "0.0.0.0", "hash" "4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b", "vin_sz" 1, "time" 1231006505, "out" [{"n" 0, "addr" "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", "spent" false, "value" 5000000000, "script" "4104678afdb0fe5548271967f1a67130b7105cd6a828e03909a67962e0ea1f61deb649f6bc3f4cef38c4f35504e51ec112de5c384df7ba0b8d578a4c702b6bf11d5fac", "addr_tag_link" "https://en.bitcoin.it/wiki/Genesis_block", "type" 0, "addr_tag" "Genesis of Bitcoin", "tx_index" 14849}], "lock_time" 0, "inputs" [{"sequence" 4294967295, "witness" "", "script" "04ffff001d0104455468652054696d65732030332f4a616e2f32303039204368616e63656c6c6f72206f6e206272696e6b206f66207365636f6e64206261696c6f757420666f722062616e6b73"}], "size" 204, "tx_index" 14849, "ver" 1, "weight" 816}], "nonce" 2083236893, "size" 285, "ver" 1, "prev_block" "0000000000000000000000000000000000000000000000000000000000000000"}]}
```
The data returned by all functions are always clojure data/data structures.

Get the destination address of the genesis block:

```clojure
=> (def genesis-addr (get-in (block-height 0) ["blocks" 0 "tx" 0 "out" 0 "addr"]))
nil
=> genesis-addr
"1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"
```

How many btc was sent to this address in USD (current market value!).
```clojure

=> (* satoshi (USD-last) ((rawaddr genesis-addr) "total_received")) 
502702.563783512
```

To see a list of API functions:
```clojure
=> (-> *ns* ns-publics keys sort)
(AUD AUD-buy AUD-last AUD-sell AUD-symbol BRL BRL-buy BRL-last BRL-sell BRL-symbol CAD CAD-buy CAD-last CAD-sell CAD-symbol CHF CHF-buy CHF-last CHF-sell CHF-symbol CLP CLP-buy CLP-last CLP-sell CLP-symbol CNY CNY-buy CNY-last CNY-sell CNY-symbol DKK DKK-buy DKK-last DKK-sell DKK-symbol EUR EUR-buy EUR-last EUR-sell EUR-symbol GBP GBP-buy GBP-last GBP-sell GBP-symbol HKD HKD-buy HKD-last HKD-sell HKD-symbol INR INR-buy INR-last INR-sell INR-symbol ISK ISK-buy ISK-last ISK-sell ISK-symbol JPY JPY-buy JPY-last JPY-sell JPY-symbol KRW KRW-buy KRW-last KRW-sell KRW-symbol NZD NZD-buy NZD-last NZD-sell NZD-symbol PLN PLN-buy PLN-last PLN-sell PLN-symbol RUB RUB-buy RUB-last RUB-sell RUB-symbol SEK SEK-buy SEK-last SEK-sell SEK-symbol SGD SGD-buy SGD-last SGD-sell SGD-symbol THB THB-buy THB-last THB-sell THB-symbol TWD TWD-buy TWD-last TWD-sell TWD-symbol USD USD-buy USD-last USD-sell USD-symbol _24hrbtcsent _24hrprice _24hrtransactioncount addressbalance addressfirstseen addresstohash addrpubkey avgtxnumber avgtxsize avgtxvalue bcperblock block-height blockchain-query-call blocks charts dbg def-address-lookup def-query def-rates eta getblockcount getdifficulty getreceivedbyaddress getsentbyaddress hashestowin hashpubkey hashrate hashtoaddress interval latestblock latesthash marketcap nextretarget pools probability pubkeyaddr rate-symbols- rawaddr rawblock rawtx rejected satoshi stats ticker tobtc totalbc transactions txfee txresult txtotalbtcinput txtotalbtcoutput unconfirmedcount)
```

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
