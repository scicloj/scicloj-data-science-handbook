(ns scicloj.helpers.datasets
  (:require [fastmath.random :as random]
            [tech.v3.tensor :as tensor]
            [tablecloth.api :as tablecloth]
            [tech.v3.datatype.functional :as dtype-fun :refer
           [+ - * /]]
            )
  )

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





(defn make-blob ; mimicking the python make_blob function
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


(comment

  (-> (random/rng :isaac 1337)
      (random-center))

  (-> (random/rng :isaac 1337)
      (random-point))

  (let [rng    (random/rng :isaac 1337)
        center [30 -40]
        n-samples 100
        std 10]
    (-> (repeatedly n-samples #(random-point rng))
        tensor/->tensor
        (* std)
        (+ (tensor/broadcast center [n-samples 2]))))

 (let [rng    (random/rng :isaac 1337)
      center [30 -40]]
  [center (random-points-around-center rng 12 center 3)])
  )
