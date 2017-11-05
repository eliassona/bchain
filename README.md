# bchain

A Clojure API for blockchain.info

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

=> (* satoshi ((USD) "last") ((rawaddr genesis-addr) "total_received")) ;how many btc was sent to this address in USD (current market value!).
502702.563783512
```


## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
