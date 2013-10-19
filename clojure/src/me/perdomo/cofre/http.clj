(ns me.perdomo.cofre.http
  (:require [clojure.java.io :as io :only (copy file)] 
            [ring.middleware.params :refer (wrap-params)]
            [ring.middleware.multipart-params :refer (wrap-multipart-params)]
            [ring.adapter.jetty :refer (run-jetty)]
            [ring.util.response :refer (response content-type not-found file-response)]
            [compojure.core :refer (defroutes GET POST)]
            [compojure.route :refer (resources)]
            [hiccup.page :refer (html5)])
  (:gen-class))

(defn- get-file-name []
  (Long/toString (System/nanoTime) 36))

(defn- copy-file [tempfile]
  (let [fname (get-file-name)
        path (io/file "/var/tmp/photos")
        newfile (io/file (format "%s/%s.jpg" path fname))]
    (.mkdirs path)
    (io/copy tempfile newfile)
    fname))

(defn upload [{params :multipart-params}]
  (let [image (params "image")
        id (copy-file (image :tempfile))]
    (response id)))

(defn get-image [id]
  (let [image (io/file (format "/var/tmp/photos/%s.jpg" id))]
    (if-not (.exists image)
      (not-found "Image not found")
      (-> (file-response (.getAbsolutePath image))
        (content-type "image/jpeg")))))

(defn get-html [id]
  (html5
    [:head
      [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
      [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf8"}]
      [:meta {:name "apple-mobile-web-app-capable" :content "yes"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0, maximum-scale=1.0"}]
      [:link {:rel "stylesheet" :href "public/pure-min.css"}]
      [:title ""]]
    [:body
     [:div {:style "width: 100%; height: 100%; position: absolute; left: 0px; top: 0px; z-index: 0;"}
      [:img {:src (str "img/" id) :style "min-width:100%; min-height:100%; width:auto; height:auto;"}]]]))

(defroutes routes
  (GET "/" req "Hi there!")
  (GET "/:id" [id] (get-html id))
  (GET "/img/:id" [id] (get-image id))
  (resources "/public")
  (POST "/" req (upload req)))

(def app
  (-> routes
    wrap-params
    wrap-multipart-params))

(defn -main []
  (run-jetty #'app {:join? false
                    :port 3000}))
