(ns metrics-server.checkstyle
  (:require [clojure.xml :as c-xml]
            [clojure.data.xml :as c-d-xml :refer [parse parse-str]]
            [clojure.zip :as c-zip :refer [xml-zip down up right left node]]
            [clojure.data.zip :as c-d-zip]
            [clojure.data.zip.xml :as c-d-z-xml :refer [xml-> xml1-> attr attr= text]]
            [clojure.java.io :as io :refer [file]]
            [clojure.pprint :refer [pprint]])
  (:import (java.io File)))

(defn as-int [s]
  (println ">" s "<")
  (Integer/parseInt (clojure.string/replace s "," "")))

(defn re-parser [re & names]
  (fn [s]
    (if-let [matches (re-find re s)]
      (zipmap names
              (map as-int (rest matches)))
      (throw (Exception. (str "no mach for " s " in " re))))))

(defn existence-parser [str]
  (fn [s]
     {  :value (if (= s str) 1 0)
        :max   1}))

(def parsers {
              "com.puppycrawl.tools.checkstyle.checks.metrics.ClassFanOutComplexityCheck"
              (re-parser #"Class Fan-Out Complexity is (\d+) \(max allowed is (\d+)\)" :value :max),
              "com.puppycrawl.tools.checkstyle.checks.coding.NestedIfDepthCheck"
              (re-parser #"Nested if-else depth is (\d+) \(max allowed is (\d+)\)" :value :max),
              "com.puppycrawl.tools.checkstyle.checks.sizes.FileLengthCheck"
              (re-parser #"File length is ((?:\d|,)+) lines \(max allowed is (\d+)\)" :value :max),
              "com.puppycrawl.tools.checkstyle.checks.sizes.AnonInnerLengthCheck"
              (re-parser #"Anonymous inner class length is (\d+) lines \(max allowed is (\d+)\)" :value :max),
              "com.puppycrawl.tools.checkstyle.checks.metrics.CyclomaticComplexityCheck"
              (re-parser #"Cyclomatic Complexity is (\d+) \(max allowed is (\d+)\)" :value :max),
              "com.puppycrawl.tools.checkstyle.checks.metrics.BooleanExpressionComplexityCheck"
              (re-parser #"Boolean expression complexity is (\d+) \(max allowed is (\d+)\)" :value :max),
              "com.puppycrawl.tools.checkstyle.checks.sizes.MethodLengthCheck"
              (re-parser #"Method length is (\d+) lines \(max allowed is (\d+)\)" :value :max),
              "com.puppycrawl.tools.checkstyle.checks.coding.MissingSwitchDefaultCheck"
              (existence-parser "switch without \"default\" clause."),
              "com.puppycrawl.tools.checkstyle.checks.sizes.ParameterNumberCheck"
              (re-parser #"More than (\d+) parameters \(found (\d+)\)" :max :value),
              "com.puppycrawl.tools.checkstyle.checks.metrics.ClassDataAbstractionCouplingCheck"
              (re-parser #"Class Data Abstraction Coupling is (\d+) \(max allowed is (\d+)\)" :value :max)
              })

(defn accumulate-errors [{:keys [tag attrs] :as error}]
  (let [{:keys [message source]} attrs]
    (if-let [parser (get parsers source)]
      (merge
        {:name (last (clojure.string/split source #"\."))}
        (parser message))
      (throw (Exception. (str "No parser for " source))))))


(defn reduce-error [memo {:keys [name value max] :as error}]
  (pprint error)
  (if (contains? memo name)
    (update memo name + (/ value max))
    (assoc memo name (/ value max))))

(defn summarize-errors [errors]
  (let [by-name (reduce reduce-error {} errors)]
    (assoc by-name "Total" (reduce + (vals by-name)))))

(defn checkstyle-errors [^File checkstyle-output]
  (let [xml (parse-str (slurp (.getPath checkstyle-output)))]
    (into {}
          (for [nz (xml-> (xml-zip xml) :file)
                :let [n (node nz)
                      kids (:content n)]
                :when (not (empty? kids))]
            (do (prn (-> n :attrs :name))
              [(.getCanonicalPath (file (-> n :attrs :name)))
               (-> (map accumulate-errors kids)
                   summarize-errors)
               ])))))


(comment

  (= (:tag x) :checkstyle)
  (def filename "samples/spring-boot.xml")

(pprint (checkstyle-errors (file filename)))


  (pprint
    (for [nz (xml-> (xml-zip x) :file)
          :let [n (node nz)
                kids (:content n)]
          :when (not (empty? kids))]
      {:file (-> n :attrs :name)
       :errors (map accumulate-errors kids)}

      )
    )

  (pprint (into {}
                (for [nz (xml-> (xml-zip x) :file :error)
                      :let [n (node nz)
                            {:keys [message source]} (:attrs n)]]

                  [source message])))

  )
