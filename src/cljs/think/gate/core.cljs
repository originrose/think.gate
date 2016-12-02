(ns think.gate.core
  (:require [reagent.core :refer [atom] :as reagent]))

(enable-console-print!)

(def started* (atom false))
(def component* (atom [:div "Welcome to think.gate... Did you call think.gate.core/set-component from cljs?"]))

(defn current-page
  []
  [:div.page-wrapper @component*])

(defn set-component
  [component]
  (reset! component* component)
  (when-not @started*
    (reset! started* true)
    (reagent/render
     [current-page]
     (.getElementById js/document "app"))))
