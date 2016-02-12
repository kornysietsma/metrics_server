(ns metrics-server.handler
  (:require [compojure.core :refer :all]

            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.json :refer [wrap-json-response]]
            [metrics-server.sourcetree :as sourcetree]
            [clojure.pprint :refer [pprint]]))

(defroutes app-routes
           (GET "/" [] "This is an API - you need the metrics ui to view it!")
           (GET "/data/metrics.json" []
             {:body (sourcetree/scan-metrics sourcetree/source_root)})
           (route/not-found "Not Found"))

(def app
  (-> app-routes

      (wrap-defaults site-defaults)
      wrap-json-response))
