(ns noir.fetch.remotes
  (:require [clojure.edn :as edn])
  (:use [noir.core :only [defpage]]))

(def remotes (atom {}))

(defn get-remote [remote]
  (get @remotes remote))

(defn add-remote [remote func]
  (swap! remotes assoc remote func))

(defn safe-read [s]
  (edn/read-string s))

(defmacro defremote [remote params & body]
  `(do
    (defn ~remote ~params ~@body)
    (add-remote ~(keyword (name remote)) ~remote)))

(defn call-remote [remote params]
  (if-let [func (get-remote remote)]
    (let [result (apply func params)]
      {:status 202
       :headers {"Content-Type" "application/clojure; charset=utf-8"}
       :body (pr-str result)})
    {:status 404}))

(defn wrap-remotes [handler]
  (println "*** fetch/wrap-remotes is no longer needed. Please remove it ***")
  handler)

(defpage [:any "/_fetch"] {:keys [remote params]}
  (let [params (safe-read params)
        remote (keyword remote)]
    (call-remote remote params)))
