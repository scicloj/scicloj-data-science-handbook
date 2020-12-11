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

["## Looking into the Iris dataset"]

^kind/dataset
data/iris

["Visualizing:"]


(require '[clojure.java.io :as io]
         '[tablecloth.api :as tablecloth]
         '[tech.viz.vega :as viz]
         '[aerial.hanami.common :as hc]
         '[scicloj.data :as data]
         '[scicloj.helpers :as helpers]
         '[scicloj.helpers.vega :as helpers.vega])

^kind/vega
(-> data/iris
    (tablecloth/rows :as-maps)
    (viz/scatterplot :sepal-width :sepal-length))

^kind/vega
(hc/xform
 helpers.vega/interactive-scatterplot-matrix
 :VALDATA (-> data/iris
              (tablecloth/rows :as-maps))
 :ROWS [:sepal-length :sepal-width :petal-length :petal-width],
 :COLUMNS [:sepal-length :sepal-width :petal-length :petal-width]
 :COLOR-FIELD :species)
