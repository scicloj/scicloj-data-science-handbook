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
         '[tech.v3.dataset.modelling :as ds-mod]
         '[tech.v3.dataset :as ds])


(def blob
  (-> (random/rng :isaac 1123)
      (make-blob 300 4 1)))


(def blob
  (-> blob
      (ds/add-column
       (-> (ds/new-column :_i
                          (map #(str "_" %) (blob :i))
                          {:categorical? true})))
      (tablecloth/drop-columns :i)
      (ds-mod/set-inference-target :_i)
      (ds/categorical->number [:_i])))


^kind/dataset-grid blob

(def trained-model
  (ml/train blob
            {:model-type
             :smile.classification/decision-tree}))

;; (def trained-random
;;   (ml/train blob
;;             {:model-type
;;              :smile.classification/random-forest}))



(defn column-range
  [ds column step]
  (range (apply min (get ds column))
         (apply max (get ds column))
         step))


(def grid

  (for [x (column-range blob :x 0.1)
        y (column-range blob :y 0.4)
        ]
    {:x x :y y}

    ))

(def grid
  (tablecloth/dataset
   {:x (map :x grid)
    :y (map :y grid)
    }
   )
  )



(def prediction-grid
  (-> (ml/predict grid trained-model)
      (tablecloth/select-columns :_i)
      (ds-mod/column-values->categorical :_i)))

(def grid-with-preds
  (tablecloth/add-or-replace-column grid :i prediction-grid))

^kind/dataset-grid
grid-with-preds


^kind/vega
(viz/scatterplot (tablecloth/rows grid-with-preds :as-maps)
                 :x
                 :y
                 {:label-key :i})


(require '[aerial.hanami.common :as hanami-common]
         '[aerial.hanami.templates :as hanami-templates])

(defn hanami-plot
  "Syntactic sugar for hanami plots, lets you pipe data directly in a thread first macro"
  [data template & substitutions]
  (apply hanami-common/xform
         template
         :DATA
         data
         substitutions))

^kind/vega
(-> grid-with-preds
    (tablecloth/rows :as-maps)
    (hanami-plot hanami-templates/point-chart))




