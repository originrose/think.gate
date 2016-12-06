(ns think.gate.core
  (:require [org.httpkit.server :as server]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.resource :refer [wrap-resource]]
            [hiccup.page :as hiccup]
            [figwheel-sidecar.repl-api :refer [start-figwheel! stop-figwheel!]]
            [clojure.string :as string]
            [mikera.image.core :as imagez])
  (:import [java.awt.image BufferedImage]
           [java.io ByteArrayOutputStream ByteArrayInputStream]))

(defonce gate* (atom nil))

(def base-page
  '([:head]
    [:body
     [:div#app "Loading think.gate..."]
     [:script {:src "js/app.js"}]]))


(defn image->input-stream
  [^BufferedImage img]
  (let [data-stream (ByteArrayOutputStream.)]
    (imagez/write img data-stream "PNG")
    (ByteArrayInputStream. (.toByteArray data-stream))))


(defn- ok
  [body]
  (if (instance? BufferedImage body)
    {:status 200
     :body (image->input-stream body)
     :headers {"Content-Type" "image/png"}}
    {:status 200
     :body body}))

(defn close
  []
  (if-let [stop-fn @gate*]
    (stop-fn))
  :gate-closed)


(defn parse-query-string
  [query-string]
  (when query-string
    (let [parts (string/split query-string #"&")]
      (->> parts
           (map (fn [query-part]
                  (let [[k v] (string/split query-part #"=")]
                    [(keyword k) v])))
           (into {})))))


(defn main-handler
  [routing-map req]
  (try
   (cond
     (and (= (:uri req) "/")
          (empty? (:params req))) (ok (hiccup/html5 base-page))
     :else
     (if-let [handler (get @routing-map (.substring (get req :uri) 1))]
       (ok (handler (merge (get req :params)
                           (parse-query-string (get req :query-string)))))
       {:status 404}))
   (catch Throwable e
     (clojure.pprint/pprint e)
     e)))


(defn handler-stack
  [handler]
  (fn [req]
   (try
     (handler req)
     (catch Throwable e
       (clojure.pprint/pprint e)
       (throw e)))))

(defn open
  [routing-map & {:keys [port]
                  :or {port 8090}}]
  (close)
  (start-figwheel!)
  (let [stop-server (-> (fn [req]
                          (main-handler routing-map req))
                        (wrap-resource "public")
                        (wrap-restful-format)
                        handler-stack
                        (server/run-server {:port port}))]
    (reset! gate* (fn []
                    (stop-figwheel!)
                    (stop-server)
                    :all-stopped)))
  "Gate opened on http://localhost:8090")
