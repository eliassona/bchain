(defproject bchain "0.1.0-SNAPSHOT"
  :description "blockchain.info API"
  :url "https://github.com/eliassona/bchain"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 [redis.clients/jedis "2.9.0"]
                 [org.clojure/data.json "0.2.4"]])
