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
  (->> variable-map
       (map (fn [[k v]]
              (format "%s=\"%s\"" (.replace (name k) "-" "_") v)))
       (string/join ";\n")))

(defn base-page
  [variable-map]
  (list
   [:head
    [:link {:type "text/css" :href "css/app.css" :rel "stylesheet"}]
    (when-not (empty? variable-map)
      [:script {:type "text/javascript"} (variable-map->string variable-map)])]
   [:body
    [:div#app "Loading think.gate... Does your cljs namespace call think.gate.core/start-frontend?"]
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
  [req routing-map variable-map]
  (let [^String uri (:uri req)]
    (cond
      (and (= uri "/")
           (empty? (:params req))) (ok (hiccup/html5 (base-page variable-map)))
      :else
      (if-let [handler (get @routing-map (.substring uri 1))]
        (ok (handler (merge (get req :params)
                            (parse-query-string (get req :query-string)))))
        {:status 404}))))

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
  (try
    (require 'css.styles :reload)
    (let [css-file-path "resources/public/css/app.css"]
      (io/make-parents css-file-path)
      (spit css-file-path (apply garden/css @(resolve 'css.styles/styles))))
    (catch Throwable e
      (println "=============================")
      (println "Exception while updating css:")
      (println " " (.getMessage e))
      (println ""))))

(defn start-css!
  [css-input-path]
  (if (.exists (io/file css-input-path))
    (let [stop-chan (chan)
          done?* (atom false)
          modified-namespaces (ns-tracker [css-input-path])]
      (css-update!)
      (thread (while (not @done?*)
                (let [timeout-chan (timeout 250)
                      [_ c] (alts!! [timeout-chan stop-chan])]
                  (if (= c timeout-chan)
                    (if (some #{'css.styles} (modified-namespaces))
                      (css-update!))
                    (reset! done?* true)))))
      #(>!! stop-chan "stop"))
    (println "No css detected. If you would like css please add a namespace `css.styles` with a var named `styles`.")))


(defn open
  "Open up the gate server.  It will serve up data from resources/public.
A set of static global variables accessed through (aget js/window varname)
is passed in through variable map, all values are encoded as strings.
There is a choice of where the system looks for css for the live updating"
  [routing-map & {:keys [port variable-map clj-css-path live-updates?]
                  :or {port 8090
                       variable-map {:render-page "default"}
                       clj-css-path "src/clj/css"
                       live-updates? true}}]
  (close)
  (when live-updates?
    (start-figwheel!))
  (let [stop-server (-> (fn [request]
                          (main-handler request routing-map variable-map))
                        (wrap-resource "public")
                        (wrap-restful-format)
                        (wrap-report-errors)
                        (server/run-server {:port port}))
        stop-css! (when live-updates?
                    (start-css! clj-css-path))]
    (reset! gate* (fn []
                    (when stop-css!
                      (stop-css!))
                    (when live-updates?
                      (stop-figwheel!))
                    (stop-server)
                    :all-stopped)))
  (format "Gate opened on http://localhost:%s" port))
