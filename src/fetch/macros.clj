(ns fetch.macros)

(defmacro remote
  [[sym & params] & [destruct & body]]
  (let [func (if destruct
               `(fn ~destruct ~@body)
               nil)]
    `(fetch.remotes/remote-callback ~(name sym)
                                    ~(vec params)
                                    ~func)))

(defmacro letrem
  [bindings & body]
  (let [bindings (partition 2 bindings)]
    (reduce
      (fn [prev [destruct func]]
        `(remote ~func [~destruct] ~prev))
      `(do ~@body)
      (reverse bindings))))


