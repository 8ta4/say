(defproject clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[cheshire "5.12.0"]
                 [clj-http "3.12.3"]
                 [clj-python/libpython-clj "2.025"]
                 [com.rpl/specter "1.1.4"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/core.async "1.6.681"]
                 [ring "1.10.0"]]
  :main ^:skip-aot clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
