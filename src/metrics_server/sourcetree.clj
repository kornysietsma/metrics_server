(ns metrics-server.sourcetree
  (:require [clojure.java.io :refer [file]]
            [clojure.zip :refer [zipper]]
            [clojure.walk :as walk]
            [clojure.pprint :refer [pprint]]
            [metrics-server.cloc :refer [read-cloc]]
            [metrics-server.checkstyle :refer [checkstyle-errors]]
            )
  (:import (java.io File)))

(defn scan-tree [^File f]
  (if (.isDirectory f)
    {:file     f
     :children (map scan-tree (.listFiles f))
     :data     {}
     }
    {:file f
     :data {}
     }
    )
  )

(defn update-in-ftree [ftree key fn branchfn]
  (if-let [kids (:children ftree)]
    (let [newtree (assoc ftree :children (map #(update-in-ftree % key fn branchfn) kids))
          result (assoc-in newtree [:data key] (branchfn newtree))]
      result)
    (assoc-in ftree [:data key] (fn (:file ftree)))))

(defn sizeof [node]
  (get-in node [:data :size]))

(defn add-sizes [ftree]
  (update-in-ftree ftree :size
                   #(.length %)
                   (fn [n] (reduce + 0 (map sizeof (:children n))))
                   ))

(defn add-data-from-map [ftree m key]
  "update tree with data from a map of the form {canonical filename : {stuff}}"
  (update-in-ftree ftree key
                   (fn [f]
                      (get m (.getCanonicalPath f) {}))
                   (constantly nil)))

(defn ftree->flare [{:keys [file children data] :as ftree}]
  (let [converted
        {:name (.getName file)
                :data data}]
    (if children
      (assoc converted :children (map ftree->flare children))
      converted )))

(def source_root (file "/Users/korny/sources/java/spring/spring-boot-trunk/spring-boot"))

(def mem-cloc (memoize read-cloc))

(def checkstyle-output (file "samples/spring-boot.xml")) ; TODO: automatically run checkstyle!

(def mem-checkstyle (memoize checkstyle-errors))

(defn scan-metrics [r]
  (let [clocdata (mem-cloc r)
        checkdata (mem-checkstyle checkstyle-output)]
    (-> r
        scan-tree
        add-sizes
        (add-data-from-map clocdata :cloc)
        (add-data-from-map checkdata :checkstyle)
        ftree->flare)))

(comment

  (def clocdata (read-cloc source-root))

  (-> root
      scan-tree
      add-sizes
      (add-data-from-map clocdata :cloc)
      ftree->flare
      clojure.pprint/pprint)
  )