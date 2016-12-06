(defproject simple "0.1.0-SNAPSHOT"
  :description "Simplest possible example project using thinktopic/gate"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [thinktopic/gate "0.1.1-SNAPSHOT"]]

  :source-paths ["src/clj"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :cljsbuild {:builds
              [{:id "dev"
                :figwheel true
                :source-paths ["src/cljs"]
                :compiler {:main "simple.gate-frontend"
                           :asset-path "out"
                           :output-to "resources/public/js/app.js"
                           :output-dir "resources/public/out"}}]}

  :main ^:skip-aot simple.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
