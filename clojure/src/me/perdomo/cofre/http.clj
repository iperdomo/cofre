(ns me.perdomo.cofre.http
  (:require [clojure.java.io :as io :only (copy file)]
            [clojure.edn :as edn :only (read-string)]
            [ring.middleware.params :refer (wrap-params)]
            [ring.middleware.multipart-params :refer (wrap-multipart-params)]
            [ring.adapter.jetty :refer (run-jetty)]
            [ring.util.response :refer (response content-type not-found file-response)]
            [compojure.core :refer (defroutes GET POST)]
            [compojure.route :refer (resources)]
            [hiccup.page :refer (html5)])
  (:import java.text.SimpleDateFormat java.util.Date
           javax.imageio.ImageIO
           org.apache.commons.io.FileUtils
           org.apache.commons.codec.binary.Base64)
  (:gen-class))

(def config (atom nil))

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

(defn image-data
  [f]
  (let [bf (ImageIO/read f)]
    {:width (.getWidth bf)
     :height (.getHeight bf)
     :b64 (Base64/encodeBase64String (FileUtils/readFileToByteArray f))}))

(defn get-html [id]
  (let [f (io/file (format "/var/tmp/photos/%s.jpg" id))]
    (if (.exists f)
      (let [data (image-data f)
            cfg @config
            cr (format "&copy; %s %s"(.format (SimpleDateFormat. "yyyy") (Date. (.lastModified f))) (:footer cfg))]
        (html5
          [:head
           [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
           [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf8"}]
           [:meta {:name "apple-mobile-web-app-capable" :content "yes"}]
           [:link {:rel "stylesheet" :href "public/pure-min.css"}]
           [:title ""]]
          [:body {:style "background: #edeeef; color: #517fa4;; text-align: center;"}
           [:div (:header cfg)]
           [:div
            [:div {:style (format "margin:0 auto;width:%spx;height:%spx;background:url(data:image/jpeg;base64,%s);" (:width data) (:height data) (:b64 data))}]]
           [:div cr]]))
      (not-found "Image not available"))))

(defroutes routes
  (GET "/" req "Hi there!")
  (GET "/:id" [id] (get-html id))
  (resources "/public")
  (POST "/" req (upload req))
  (not-found "Not found"))

(def app
  (-> routes
    wrap-params
    wrap-multipart-params))

(defn -main []
  (reset! config (-> "config.edn" io/resource slurp edn/read-string))
  (run-jetty #'app {:join? false
                    :port 3000}))
