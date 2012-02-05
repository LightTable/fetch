(ns fetch.remotes
  (:require [fetch.core :as core]
            [cljs.reader :as reader]))

(def remote-uri "/pinotremotecall")

(defn remote-callback [remote params callback]
  (core/xhr [:post remote-uri] 
            (pr-str {:remote remote
                     :params params})
            (when callback
              (fn [data]
                (callback (reader/read-string data))))
            {"Content-Type" 
             "application/clojure;charset=utf-8"}))
