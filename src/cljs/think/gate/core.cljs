(ns think.gate.core
  (:require [reagent.core :refer [atom] :as reagent]))

(enable-console-print!)

(def started* (atom false))
(def frontend* (atom [:div "Welcome to think.gate..."]))


(defmulti component (fn [& args] (first args)))

(defmethod component :default
  [& args]
  (fn [& args]
    [:div (str "Welcome to think.gate... Unrecognized render args: " args)]))


(defn current-page
  []
  [:div.page-wrapper @frontend*])

(defn- set-frontend
  [frontend]
  (reset! frontend* frontend)
  (when-not @started*
    (reset! started* true)
    (reagent/render
     [current-page]
     (.getElementById js/document "app"))))

(defn start-frontend
  []
  (set-frontend [(component (aget js/window "render_page"))]))
