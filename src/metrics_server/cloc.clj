(ns metrics-server.cloc
  (:require
    [clojure.java.io :refer [file]]
    [me.raynes.conch :refer [programs with-programs let-programs] :as sh]
    [clj-yaml.core :as yaml]
    [clojure.walk :refer [keywordize-keys]]
    [clojure.pprint :refer [pprint]])
  (:import (java.io File)))

(defn- canonical-keys-and-keyword-keys [m]
  (into {}
        (for [[k v] m]
          [(.getCanonicalPath (file k)) (keywordize-keys v)])))

(defn read-cloc [^File root]
  (with-programs [cloc]
                 (->
                   (cloc (.getAbsolutePath root) "--by-file" "--yaml" "--quiet")
                   (yaml/parse-string false)
                   canonical-keys-and-keyword-keys
                   )

                 ))

