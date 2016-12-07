(ns simple.core
  (:require [think.gate.core :as gate])
  (:gen-class))

(defn on-foo
  [params] ;; consider destructuring your arguments
  (+ (:a params) 41))

(def routing-map
  {"foo" #'on-foo})

(defn gate
  []
  (gate/open #'routing-map))


(defn labeller
  []
  (gate/open #'routing-map :variable-map {:render-page "otherthing"}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println (gate)))
