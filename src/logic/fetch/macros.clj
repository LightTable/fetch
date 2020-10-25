(ns logic.fetch.macros
  "Functions/Macros to work with partials and pages.
  Extracted from noir source code."
  (:require [clojure.string :as string]
            [clojure.tools.macro :as macro]
            [compojure.core]))

(defonce noir-routes (atom {}))
(defonce route-funcs (atom {}))
(defonce pre-routes (atom (sorted-map)))
(defonce post-routes (atom []))
(defonce compojure-routes (atom []))

(defn- keyword->symbol [namesp kw]
  (symbol namesp (string/upper-case (name kw))))

(defn- route->key [action rte]
  (let [action (string/replace (str action) #".*/" "")]
    (str action (-> rte
                    (string/replace #"\." "!dot!")
                    (string/replace #"/" "--")
                    (string/replace #":" ">")
                    (string/replace #"\*" "<")))))

(defn- throwf [msg & args]
  (throw (Exception. (apply format msg args))))

(defn- parse-fn-name [[cur :as all]]
  (let [[fn-name remaining] (if (and (symbol? cur)
                                     (or (@route-funcs (keyword (name cur)))
                                         (not (resolve cur))))
                              [cur (rest all)]
                              [nil all])]
    [{:fn-name fn-name} remaining]))

(defn- parse-route [[{:keys [fn-name] :as result} [cur :as all]] default-action]
  (let [cur (if (symbol? cur)
              (try
                (deref (resolve cur))
                (catch Exception e
                  (throwf "Symbol given for route has no value")))
              cur)]
    (when-not (or (vector? cur) (string? cur))
      (throwf "Routes must either be a string or vector, not a %s" (type cur)))
    (let [[action url] (if (vector? cur)
                         [(keyword->symbol "compojure.core" (first cur)) (second cur)]
                         [default-action cur])
          final (-> result
                    (assoc :fn-name (if fn-name
                                      fn-name
                                      (symbol (route->key action url))))
                    (assoc :url url)
                    (assoc :action action))]
      [final (rest all)])))

(defn- parse-destruct-body [[result [cur :as all]]]
  (when-not (some true? (map #(% cur) [vector? map? symbol?]))
    (throwf "Invalid destructuring param: %s" cur))
  (-> result
      (assoc :destruct cur)
      (assoc :body (rest all))))

(defn ^{:skip-wiki true} parse-args
  "parses the arguments to defpage. Returns a map containing the keys :fn-name :action :url :destruct :body"
  [args & [default-action]]
  (-> args
      (parse-fn-name)
      (parse-route (or default-action 'compojure.core/GET))
      (parse-destruct-body)))

(defmacro defpage
  "Adds a route to the server whose content is the the result of evaluating the body.
  The function created is passed the params of the request and the destruct param allows
  you to destructure that meaningfully for use in the body.

  There are several supported forms:

  (defpage \"/foo/:id\" {id :id})  an unnamed route
  (defpage [:post \"/foo/:id\"] {id :id}) a route that responds to POST
  (defpage foo \"/foo:id\" {id :id}) a named route
  (defpage foo [:post \"/foo/:id\"] {id :id})

  The default method is GET."
  [& args]
  (let [{:keys [fn-name action url destruct body]} (parse-args args)]
    `(do
       (defn ~fn-name {::url ~url
                       ::action (quote ~action)
                       ::args (quote ~destruct)} [~destruct]
         ~@body)
       (swap! route-funcs assoc ~(keyword fn-name) ~fn-name)
       (swap! noir-routes assoc ~(keyword fn-name) (~action ~url {params# :params} (~fn-name params#))))))

(defmacro defpartial
  "Create a function that returns html using hiccup. The function is callable with the given name. Can optionally include a docstring or metadata map, like a normal function declaration."
  [fname & args]
  (let [[fname args] (macro/name-with-attributes fname args)
        [params & body] args]
    `(defn ~fname ~params
       (html
        ~@body))))
