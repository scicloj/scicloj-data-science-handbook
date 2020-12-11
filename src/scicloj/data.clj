(ns scicloj.data
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]))

["# Datasets"]

["## The Iris dataset"]

(require '[clojure.java.io :as io]
         '[tablecloth.api :as tablecloth]
         '[scicloj.helpers :as helpers])

(def iris
  (-> "data/iris.csv"
      io/resource
      (.toString)
      (tablecloth/dataset {:key-fn helpers/->tidy-name})))

^kind/dataset
iris
