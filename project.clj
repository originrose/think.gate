(defproject thinktopic/think.gate "0.1.5"
  :description "A library for hacking wepages into your clojure app."

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [http-kit "2.2.0"]
                 [hiccup "1.0.5"]
                 [ns-tracker "0.3.1"]
                 [figwheel-sidecar "0.5.12"]
                 [org.clojure/clojurescript "1.9.854"]
                 [ring-middleware-format "0.7.2"]
                 [garden "1.3.2"]
                 [reagent "0.6.1"]
                 [cljs-ajax "0.6.0"]
                 [net.mikera/imagez "0.12.0"]
                 ;;Include bugfix
                 [ring/ring-core "1.5.1"]
                 [com.cognitect/transit-clj "0.8.300"]
                 [com.cognitect/transit-java "0.8.327"]
                 [cheshire "5.7.1"]]

  :repl-options {:init-ns think.gate.core}

  :source-paths ["src/clj" "src/cljs"]

  :clean-targets ^{:protect false} ["pom.xml"
                                    "target"
                                    "resources/public/out"
                                    "resources/public/js/app.js"
                                    "figwheel_server.log"]

  :cljsbuild {:builds
              [{:id "dev"
                :figwheel true
                :source-paths ["src/cljs"]
                :compiler {:main "think.gate.core"
                           :asset-path "out"
                           :output-to "resources/public/js/app.js"
                           :output-dir "resources/public/out"}}]})
