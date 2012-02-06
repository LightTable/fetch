(ns noir.fetch.remotes)

(def remote-regex #"/pinotremotecall")
(def remotes (atom {}))

(defn get-remote [remote]
  (get @remotes remote))

(defn add-remote [remote func]
  (swap! remotes assoc remote func))

(defn safe-read [s]
  (binding [*read-eval* false]
    (read-string s)))

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
  (fn [{:keys [uri body] :as req}]
    (println (slurp (:body req)))
    (if (re-seq remote-regex uri)
      (let [{:keys [remote params]} (:params req)
            params (safe-read params)
            remote (keyword remote)]
        (println "calling the remote: " remote)
        (call-remote remote params))
      (handler req))))

