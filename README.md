# thinktopic/gate

Gate is an opinionated library for hacking wepages into your clojure app.

## Usage

In your app's **project.clj**:

[![Clojars Project](https://img.shields.io/clojars/v/thinktopic/think.gate.svg)](https://clojars.org/thinktopic/think.gate)

```
;; depend on
[thinktopic/think.gate "0.1.5"]

;; add
:cljsbuild {:builds
            [{:id "dev"
              :figwheel true
              :source-paths ["src/cljs"]
              :compiler {:main "your.cljs.ns"
                         :asset-path "out"
                         :output-to "resources/public/js/app.js"
                         :output-dir "resources/public/out"}}]}

```

---

In your app's **clojure**:

```
[think.gate.core :as gate]

;; ...

(defn on-foo
  [params]
  (+ (:a params) 41))

(def routing-map
  {"foo" #'on-foo})

(defn gate
  []
  (gate/open #'routing-map))

```

---

In your app's **clojurescript** (i.e., under `src/cljs/your/cljs/ns.cljs`):

```
(ns your.cljs.ns
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [<!]]
            [reagent.core :refer [atom]]
            [think.gate.core :as gate]
            [think.gate.model :as model]))

(enable-console-print!)

(def state* (atom nil))

(defmethod gate/component "default"
  [& args]
  (fn [& args]
    (if-let [n @state*]
      [:div "Server's answer: " n]
      [:button {:on-click #(go (reset! state* (<! (model/put "foo" {:a 1})))
                               (println @state*))}
       "GO"])))

(gate/start-frontend)
```

---

For **css**, create `src/clj/css/styles.clj`:

```
(ns css.styles
  (:require [garden.units :refer [px]]))

(def styles
  [[:body {:background :green}]])
```

And add to your **project.clj**:

```
:source-paths ["src/clj"]

:figwheel {:css-dirs ["resources/public/css"]}
```

Then: [http://localhost:8090](http://localhost:8090)

## License

Copyright © 2016 ThinkTopic, LLC.
