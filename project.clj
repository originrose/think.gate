(defproject thinktopic/gate "0.1.0-SNAPSHOT"
  :description "A library for hacking wepages into your clojure app."

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.2.0"]
                 [hiccup "1.0.5"]
                 [figwheel-sidecar "0.5.8"]
                 [ring-middleware-format "0.7.0"]
                 [reagent "0.6.0"]
                 [cljs-ajax "0.5.8"]]

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
                  ["vcs" "push"]]

  :repositories  {"snapshots"  {:url "s3p://thinktopic.jars/snapshots/"
                                :passphrase :env
                                :username :env
                                :releases false
                                :sign-releases false}
                  "releases"  {:url "s3p://thinktopic.jars/releases/"
                               :passphrase :env
                               :username :env
                               :snapshots false
                               :sign-releases false}})
