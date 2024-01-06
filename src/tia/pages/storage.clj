(ns tia.pages.storage
  (:require
   [clojure.string :as cstr]
   [tia.storage :as storage]
   [tia.layout :as layout]))

(defn page [_request]
  (layout/plain
   (str "Storage connection: "
        (-> (storage/get-buckets)
            first :name))))

(def routes
  ["/storage" {:get page}])