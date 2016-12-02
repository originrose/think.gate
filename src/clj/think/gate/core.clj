(ns think.gate.core
  (:require [org.httpkit.server :as server]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.resource :refer [wrap-resource]]
            [hiccup.page :as hiccup]
            [figwheel-sidecar.repl-api :refer [start-figwheel! stop-figwheel!]]))

(defonce gate* (atom nil))

(def base-page
  '([:head]
    [:body
     [:div#app "Loading think.gate..."]
     [:script {:src "js/app.js"}]]))

(defn- ok
  [body]
  {:status 200
   :body body})

(defn close
  []
  (if-let [stop-fn @gate*]
    (stop-fn))
  :gate-closed)

(defn open
  [routing-map]
  (close)
  (start-figwheel!)
  (let [stop-server (-> (fn [req]
                          (cond
                            (and (= (:uri req) "/")
                                 (empty? (:params req))) (ok (hiccup/html5 base-page))
                            :else
                            (if-let [handler (-> req :params :method routing-map)]
                              (ok (handler (get req :params)))
                              {:status 404})))
                        (wrap-resource "public")
                        (wrap-restful-format)
                        (server/run-server))]
    (reset! gate* (fn []
                    (stop-figwheel!)
                    (stop-server)
                    :all-stopped)))
  "Gate opened on http://localhost:8090")
