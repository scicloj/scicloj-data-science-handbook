(ns scicloj.05-08-random-forests
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]))

["## Creating randomized Gaussian data"]

(require '[fastmath.random :as random]
         '[tech.v3.datatype :as dtype]
         '[tech.v3.tensor :as tensor]
         '[tech.v3.datatype.functional :as dtype-fun :refer
           [+ - * /]]
         '[tech.viz.vega :as viz]
         '[tablecloth.api :as tablecloth])
(comment
 ;; Manually start an empty notespace
 (notespace/init-with-browser)
 ;; Renders the notes and listens to file changes
 (notespace/listen)
 ;; Clear an existing notespace browser
 (notespace/init)
 ;; Evaluating a whole notespace
 (notespace/eval-this-notespace))

["Trying to mimic the make-blob function of sklearn (not sure about its implementation at the moment):"]

["https://github.com/scikit-learn/scikit-learn/blob/42aff4e2e/sklearn/datasets/_samples_generator.py#L839"]
(defn random-center
  [rng]
  (-> (repeatedly 2 #(random/drandom rng))
      vec
      (* 20)
      (- 10)))

(defn random-point
  [rng]
  (repeatedly 2 #(random/grandom rng)))

(defn random-points-around-center
  [rng n-samples center std]
  (-> (repeatedly n-samples #(random-point rng))
      tensor/->tensor
      (* std)
      (+ (tensor/broadcast center [n-samples 2]))))

(let [rng    (random/rng :isaac 1337)
      center [30 -40]]
  [center (random-points-around-center rng 12 center 3)])

(defn make-blob
  [rng n-smaples n-centers std]
  (->> #(random-center rng)
       (repeatedly n-centers)
       (map-indexed
        (fn [i center]
          (-> rng
              (random-points-around-center (quot n-smaples
                                                 n-centers)
                                           center
                                           std)
              tensor/columns
              (->> (map vec)
                   (zipmap [:x :y]))
              tablecloth/dataset
              (tablecloth/add-or-replace-column :i i))))
       (apply tablecloth/concat)))

(let [rng (random/rng :isaac 1337)] (make-blob rng 10 2 1))

^kind/vega
(let [rng  (random/rng :isaac 1123)
      data (make-blob rng 300 4 1)]
  (viz/scatterplot (tablecloth/rows data :as-maps)
                   :x
                   :y
                   {:label-key :i}))

(require '[tech.v3.ml :as ml]
         '[tech.v3.libs.smile.classification]
         '[tech.v3.libs.smile.regression]
         '[tech.v3.dataset.modelling :as ds-mod])

(def blob
  (-> (random/rng :isaac 1123)
      (make-blob 300 4 1)
      (ds-mod/set-inference-target :i)))



(def trained-model
  (ml/train blob
            {:model-type
             :smile.classification/decision-tree}))

(def trained-random
  (ml/train blob
            {:model-type
             :smile.classification/random-forest}))

(require '[aerial.hanami.common :as hanami-common]
         '[aerial.hanami.templates :as hanami-templates])

;; (defn- train
;;   [feature-ds label-ds options]
;;   (let [entry-metadata (model-type->classification-model
;;                         (model/options->model-type
;;                         options))
;;         target-colname (first (ds/column-names
;;         label-ds))
;;         feature-colnames (ds/column-names feature-ds)
;;         formula (smile-proto/make-formula
;;         (ds-utils/column-safe-name target-colname)
;;                                           (map
;;                                           ds-utils/column-safe-name
;;                                                feature-colnames))
;;         dataset (merge feature-ds
;;                        (ds/update-columnwise
;;                         label-ds :all
;;                         dtype/elemwise-cast :int32))
;;         data (smile-data/dataset->smile-dataframe
;;         dataset)
;;         properties (smile-proto/options->properties
;;         entry-metadata dataset options)
;;         ctor (:constructor entry-metadata)
;;         model (ctor formula data properties)]
;;     (model/model->byte-array model)))


;;    :decision-tree
;;    {:name :decision-tree
;;     :options [{:name :max-nodes
;;                :type :int32
;;                :default 100}
;;               {:name :node-size
;;                :type :int32
;;                :default 1}
;;               {:name :max-depth
;;                :type :int32
;;                :default 20}
;;               {:name :split-rule
;;                :type :string
;;                :lookup-table split-rule-lookup-table
;;                :default :gini}]
;;     :gridsearch-options {:max-nodes (ml-gs/linear 10
;;     1000
;;     30)
;;                          :node-size (ml-gs/linear 1 20
;;                          20)
;;                          :max-depth (ml-gs/linear 1 50
;;                          20
;;                          )
;;                          :split-rule (ml-gs/categorical
;;                          [:gini :entropy
;;                          :classification-error] )

;;                          }
;;     :property-name-stem "smile.cart"
;;     :constructor #(DecisionTree/fit ^Formula %1
;;     ^DataFrame %2  ^Properties %3)
;;     :predictor tuple-predict-posterior

;;     }
