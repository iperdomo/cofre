(ns me.perdomo.cofre.http
  (:require [clojure.java.io :as io :only (copy file)] 
            [ring.middleware.params :refer (wrap-params)]
            [ring.middleware.multipart-params :refer (wrap-multipart-params)]
            [ring.adapter.jetty :refer (run-jetty)]
            [ring.util.response :refer (response content-type not-found file-response)]
            [compojure.core :refer (defroutes GET POST)])
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

(defroutes routes
  (GET "/" req "Hi there!")
  (GET "/:id" [id] (get-image id))
  (POST "/" req (upload req)))

(def app
  (-> routes
    wrap-params
    wrap-multipart-params))

(defn -main []
  (run-jetty #'app {:join? false
                    :port 3000}))
