(ns think.gate.model
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [>! chan]]
            [ajax.core :refer [PUT]]))

(enable-console-print!)

(defn put
  ([method params]
   (let [c (chan)]
     (PUT (str "/" method) {:params params
                            :handler #(go (>! c %))})
     c))
  ([method]
   (put method {})))
