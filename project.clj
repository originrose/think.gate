(defproject thinktopic/think.gate "0.1.2"
  :description "A library for hacking wepages into your clojure app."

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.2.0"]
                 [hiccup "1.0.5"]
                 [ns-tracker "0.3.1"]
                 [figwheel-sidecar "0.5.8"]
                 [ring-middleware-format "0.7.0"]
                 [garden "1.3.2"]
                 [reagent "0.6.0"]
                 [cljs-ajax "0.5.8"]
                 [net.mikera/imagez "0.12.0"]]

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
                           :output-dir "resources/public/out"}}]}

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "" "--no-sign"] ;; disable signing
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
