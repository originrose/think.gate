(defproject simple "0.1.0-SNAPSHOT"
  :description "Simplest possible example project using thinktopic/gate"
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [thinktopic/think.gate "0.1.6"]]

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
  :profiles {:uberjar {:aot :all}}
  :clean-targets ^{:protect false} ["resources/public/css/app.css"
                                    "resources/public/js/app.js"
                                    "resources/public/out"
                                    "figwheel_server.log"])
