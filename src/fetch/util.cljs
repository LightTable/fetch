(ns fetch.util)

(defn clj->js
  "Recursively transforms ClojureScript maps into Javascript objects,
   other ClojureScript colls into JavaScript arrays, and ClojureScript
   keywords into JavaScript strings."
  [x]
  (cond
    (string? x) x
    (keyword? x) (name x)
    (map? x) (let [obj (js-obj)]
               (doseq [[k v] x]
                 (aset obj (clj->js k) (clj->js v)))
               obj)
    (coll? x) (apply array (map clj->js x))
    :else x))
