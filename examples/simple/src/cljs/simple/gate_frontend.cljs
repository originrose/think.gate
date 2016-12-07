(ns simple.gate-frontend
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


(defmethod gate/component "otherthing"
  [& args]
  (fn [& args]
   [:div "HAHAHAHAHAHAHAH"]))


(gate/start-frontend)
