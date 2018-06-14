# fetch

A ClojureScript library that makes client/server interaction painless.

## Usage

### Remotes

Remotes let you make calls to a noir server without having to think about XHR. On the client-side you simply have code that looks like this:

```clojure
(ns playground.client.test
  (:require [fetch.remotes :as remotes])
  (:require-macros [fetch.macros :as fm]))

(fm/remote (adder 2 5 6) [result]
  (js/alert result))

(fm/remote (get-user 2) [{:keys [username age]}]
  (js/alert (str "Name: " username ", Age: " age)))

;; for a much nicer experience, use letrem
(fm/letrem [a (adder 3 4)
            b (adder 5 6)]
    (js/alert (str "a: " a " b: " b)))
```

Note that the results we get are real Clojure datastructures and so we use them just as we would in normal Clojure code. No JSON here.

The noir side of things is just as simple. All you do is declare a remote using defremote.

```clojure
(use 'noir.fetch.remotes)

(defremote adder [& nums]
           (apply + nums))

(defremote get-user [id]
           {:username "Chris"
            :age 24})

(server/start 8080)
```

## License

Copyright (c) 2014 Kodowa, Inc. & Light Table contributors

Distributed under the MIT License. See LICENSE.md
