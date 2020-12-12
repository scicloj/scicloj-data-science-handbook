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

(require '[scicloj.data :as data])
^kind/dataset
data/iris

["Visualizing:"]

(require '[clojure.java.io :as io]
         '[tablecloth.api :as tablecloth]
         '[tech.viz.vega :as viz]
         '[aerial.hanami.common :as hc]
         '[aerial.hanami.templates :as ht]
         '[aerial.hanami.core :as hmi]
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

["TODO: Add histograms at the diagonal, as typical SPLOMs (scatter plot matrices) do. For Vega-Lite, it is an open issue: https://github.com/vega/vega-lite/issues/3294."]

["## Linear regression"]

(require '[tech.v3.datatype :as dtype]
         '[tech.v3.datatype.functional :as dfn]
         '[fastmath.random :as random])

["Random data:"]

^kind/hiccup-nocode
[:p/code
 "import matplotlib.pyplot as plt
import numpy as np

rng = np.random.RandomState(42)
x = 10 * rng.rand(50)
y = 2 * x - 1 + rng.randn(50)
plt.scatter(x, y);
"
 {:language :python}]

(def linear-data
  (let [n 50
        rng (random/rng :isaac 42)
        x (-> (random/->seq rng n)
              (dfn/* 10))
        y (-> x
              (dfn/* 2)
              (dfn/- 1)
              (dfn/+ (random/->seq rng n)))]
    (tablecloth/dataset
     {:x (vec x)
      :y (vec y)})))


^kind/dataset
linear-data

^kind/vega
(-> linear-data
    (tablecloth/rows :as-maps)
    (viz/scatterplot :x :y))

["Linear modeling of the data:"]

(require '[tech.v3.libs.smile.regression :as regression]
         '[tech.v3.dataset.modelling :as modelling]
         '[tech.v3.ml :as ml])

(def linear-model
  (-> linear-data
      (modelling/set-inference-target :y)
      (ml/train {:model-type :smile.regression/ridge
                 :lambda     0.00001})))

["TODO: We should use OLD (ordinary least squares here), but the tech.ml API does not wrap it yet. Ridge regression with a small `lambda` gives a close approximation."]

(ml/explain linear-model)

(def predictions-for-dummy-data
  (let [dummy-data (tablecloth/dataset {:x (range 11)})]
    (tablecloth/append dummy-data
                       (ml/predict dummy-data linear-model))))

^kind/dataset
predictions-for-dummy-data

^kind/vega
(hc/xform ht/line-chart
          :VALDATA (-> predictions-for-dummy-data
                       (tablecloth/rows :as-maps)))
