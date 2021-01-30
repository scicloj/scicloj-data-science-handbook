(ns scicloj.helpers.sklearn
  (:require [libpython-clj.python :as py]
            [tech.v3.dataset.tensor :as dst]
            [tech.v3.datatype :as dt]
            [tech.v3.tensor :as t]
            [tablecloth.api :as tc]
            [camel-snake-kebab.core :as csk]
            [tech.v3.dataset.column-filters :as cf]
            )
  )


(py/initialize! :python-executable "/home/carsten/.conda/envs/scicloj-data-science-handbook/bin/python"
                :library-path "/home/carsten/.conda/envs/scicloj-data-science-handbook/lib/libpython3.8.so")

(require '[libpython-clj.python
           :refer [as-python as-jvm
                   ->python ->jvm
                   get-attr call-attr call-attr-kw
                   get-item att-type-map
                   call call-kw initialize!
                   as-numpy ->numpy
                   run-simple-string
                   add-module module-dict
                   import-module
                   python-type
                   py. py.. py.-
                   ]]
         '[libpython-clj.require :refer [require-python]])

(require-python '[sklearn.preprocessing :refer [PolynomialFeatures StandardScaler]])

(defn snakify-keys
  "Recursively transforms all map keys from to snake case."
  {:added "1.1"}
  [m]
  (let [f (fn [[k v]] (if (keyword?  k) [(csk/->snake_case k) v] [k v]))]
    ;; only apply to maps
    (clojure.walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn fit-transform
  [ds module-kw estimator-class-kw kw-args
   ]

  (let [feature-ds (tc/select-columns ds (cf/feature ds))
        snakified-kw-args (snakify-keys kw-args)
        module (name module-kw)
        class-name (csk/->PascalCaseString estimator-class-kw)
        estimator-class-name (str "sklearn." module "." class-name)
        constructor (libpython-clj.metadata/path->py-obj estimator-class-name )
        estimator (call-kw constructor [] kw-args)
        X (-> feature-ds (dst/dataset->tensor) ->numpy)
        ]
    (->
     (py. estimator fit_transform X )
     (t/ensure-tensor)
     ;; ->jvm
     (dst/tensor->dataset)
     )
    ))

(comment

  )
