(ns think.gate.model
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [>! chan]]
            [ajax.core :refer [PUT]]))

(defn put
  ([method params]
   (let [c (chan)]
     (PUT "/" {:params (merge {:method method}
                              params)
               :handler #(go (>! c %))})
     c))
  ([method]
   (put method {})))
