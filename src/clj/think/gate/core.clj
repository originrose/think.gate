(ns think.gate.core
  (:require [clojure.core.async :refer [thread chan alts!! timeout >!!]]
            [clojure.java.io :as io]
            [org.httpkit.server :as server]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.resource :refer [wrap-resource]]
            [hiccup.page :as hiccup]
            [garden.core :as garden]
            [figwheel-sidecar.repl-api :refer [start-figwheel! stop-figwheel!]]
            [ns-tracker.core :refer [ns-tracker]]))

(defonce gate* (atom nil))

(defn base-page
  []
  (list
   [:head
    [:link {:type "text/css" :href "css/app.css" :rel "stylesheet"}]]
   [:body
    [:div#app "Loading think.gate..."]
    [:script {:type "text/javascript" :src "js/app.js"}]]))

(defn- ok
  [body]
  {:status 200
   :body body})

(defn close
  []
  (when-let [stop-fn @gate*]
    (stop-fn)
    (reset! gate* nil))
  :gate-closed)

(defn css-update!
  []
  (require 'css.styles :reload)
  (let [css-file-path "resources/public/css/app.css"]
    (io/make-parents css-file-path)
    (spit css-file-path (garden/css @(resolve 'css.styles/styles)))))

(defn start-css!
  [path]
  (let [stop-chan (chan)
        done?* (atom false)
        modified-namespaces (ns-tracker [path])]
    (thread (while (not @done?*)
              (let [timeout-chan (timeout 250)
                    [_ c] (alts!! [timeout-chan stop-chan])]
                (if (= c timeout-chan)
                  (if (some #{'css.styles} (modified-namespaces))
                    (css-update!))
                  (reset! done?* true)))))
    #(>!! stop-chan "stop")))

(defn open
  [routing-map & {:keys [css-path]}]
  (close)
  (start-figwheel!)
  (let [stop-server (-> (fn [req]
                          (cond
                            (and (= (:uri req) "/")
                                 (empty? (:params req))) (ok (hiccup/html5 (base-page)))
                            :else
                            (if-let [handler (-> req :params :method routing-map)]
                              (ok (handler (get req :params)))
                              {:status 404})))
                        (wrap-resource "public")
                        (wrap-restful-format)
                        (server/run-server))
        stop-css! (if css-path (start-css! css-path))]
    (reset! gate* (fn []
                    (if stop-css! (stop-css!))
                    (stop-figwheel!)
                    (stop-server)
                    :all-stopped)))
  "Gate opened on http://localhost:8090")
