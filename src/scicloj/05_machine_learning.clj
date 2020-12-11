(ns scicloj.05-machine-learning
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]))

;; Notespace
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Listen for changes in the namespace and update notespace automatically
;; Hidden kinds should not show in the notespace page
^kind/hidden
(comment
  ;; Manually start an empty notespace
  (notespace/init-with-browser)

  ;; Renders the notes and listens to file changes
  (notespace/listen)

  ;; Clear an existing notespace browser
  (notespace/init)

  ;; Evaluating a whole notespace
  (notespace/eval-this-notespace)
  )

;; Chapter 05 - Machine learning
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;

["# Chapter 05 - Machine Learning"]

["This chapter will dive into practical aspects of machine learning using the Clojure libraries and tools (TODO).

Some parts of it are based on code from the [Python Data Science Handbook](https://github.com/jakevdp/PythonDataScienceHandbook) by Jake VanderPlas."]

["## Intro"

 "Getting the Iris dataset"]

(require '[clojure.java.io :as io]
         '[tablecloth.api :as tablecloth]
         '[scicloj.helpers :as helpers])

(def iris-dataset
  (-> "data/iris.csv"
      io/resource
      (.toString)
      (tablecloth/dataset {:key-fn helpers/->tidy-name})))

^kind/dataset
iris-dataset

["Looking into the Iris data visually"]

(require '[tech.viz.vega :as viz])

^kind/vega
(-> iris-dataset
    (tablecloth/rows :as-maps)
    (viz/scatterplot :sepal-width :sepal-length))
