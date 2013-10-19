(defproject me.perdomo.cofre "0.2.0"
  :description "Small HTTP server to process image upload/get requests"
  :url "http://github.com/iperdomo/cofre"
  :license {:name "Apache Public License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-jetty-adapter "1.2.0"]
                 [compojure "1.1.5"]
                 [hiccup "1.0.4"]]

  :main me.perdomo.cofre.http
  :aot [me.perdomo.cofre.http])
