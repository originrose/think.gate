(ns think.gate.core
  (:require [clojure.core.async :refer [thread chan alts!! timeout >!!]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [org.httpkit.server :as server]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.middleware.resource :refer [wrap-resource]]
            [hiccup.page :as hiccup]
            [figwheel-sidecar.repl-api :refer [start-figwheel! stop-figwheel!]]
            [ns-tracker.core :refer [ns-tracker]]
            [garden.core :as garden]
            [mikera.image.core :as imagez])
  (:import [java.awt.image BufferedImage]
           [java.io ByteArrayOutputStream ByteArrayInputStream]))


(defonce gate* (atom nil))

(defn- variable-map->string
  [variable-map]
  (string/join ";" (map (fn [[k v]]
                          (let [var-name (.replace ^String (name k) "-" "_")]
                            (str var-name "=\"" v "\"")))
                        variable-map)))

(defn base-page
  [variable-map]
  (list
   [:head
    [:link {:type "text/css" :href "css/app.css" :rel "stylesheet"}]
    (when-not (empty? variable-map)
      [:script {:type "text/javascript"} (variable-map->string variable-map)])]
   [:body
    [:div#app "Loading think.gate...make sure your main cljs namespace calls think.gate.core/start-frontend"]
    [:script {:type "text/javascript" :src "js/app.js"}]]))

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
  (when-let [stop-fn @gate*]
    (stop-fn)
    (reset! gate* nil))
  :gate-closed)

(defn parse-query-string
  [query-string]
  (when query-string
    (->> (string/split query-string #"&")
         (map (fn [query-part]
                (let [[k v] (string/split query-part #"=")]
                  [(keyword k) v])))
         (into {}))))

(defn main-handler
  [routing-map req render-arg]
  (cond
    (and (= (:uri req) "/")
         (empty? (:params req))) (ok (hiccup/html5 (base-page render-arg)))
    :else
    (if-let [handler (get @routing-map (.substring (get req :uri) 1))]
      (ok (handler (merge (get req :params)
                          (parse-query-string (get req :query-string)))))
      {:status 404})))

(defn wrap-report-errors
  [handler]
  (fn [req]
   (try
     (handler req)
     (catch Throwable e
       (clojure.pprint/pprint e)
       (throw e)))))

(defn css-update!
  []
  (require 'css.styles :reload)
  (let [css-file-path "resources/public/css/app.css"]
    (io/make-parents css-file-path)
    (spit css-file-path (garden/css @(resolve 'css.styles/styles)))))

(defn start-css!
  []
  (let [stop-chan (chan)
        done?* (atom false)
        modified-namespaces (ns-tracker ["src/clj/css"])]
    (css-update!)
    (thread (while (not @done?*)
              (let [timeout-chan (timeout 250)
                    [_ c] (alts!! [timeout-chan stop-chan])]
                (if (= c timeout-chan)
                  (if (some #{'css.styles} (modified-namespaces))
                    (css-update!))
                  (reset! done?* true)))))
    #(>!! stop-chan "stop")))

(defn open
  [routing-map & {:keys [port variable-map]
                  :or {port 8090
                       variable-map {:render-page "default"}}}]
  (close)
  (start-figwheel!)
  (let [stop-server (-> #(main-handler routing-map % variable-map)
                        (wrap-resource "public")
                        (wrap-restful-format)
                        (wrap-report-errors)
                        (server/run-server {:port port}))
        stop-css! (start-css!)]
    (reset! gate* (fn []
                    (stop-css!)
                    (stop-figwheel!)
                    (stop-server)
                    :all-stopped)))
  "Gate opened on http://localhost:8090")
