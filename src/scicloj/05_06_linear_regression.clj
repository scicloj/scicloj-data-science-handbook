(ns scicloj.05-06-linear-regression
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]))

;; Notespace
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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

;; Chapter 05 - Machine learning gg
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;

["# In Depth: Linear Regression"]

["This chapter uses python interop via libpython-clj"]

["You need to initalize it correctling with a python environment containing python module 'scikit-learn'"]


(comment
  (require '[libpython-clj.python :as py])
  ;; When you use conda, it should look like this.
 (py/initialize! :python-executable "/opt/anaconda3/envs/my_env/bin/python3.7"
                 :library-path "/opt/anaconda3/envs/my_env/lib/libpython3.7m.dylib")
  )

(require '[clojure.java.io :as io]
         '[clojure.string :as str]
         '[tech.v3.dataset :as dataset]
         '[tech.v3.dataset.categorical :as ds-cat]
         '[tech.v3.dataset.column-filters :as cf]
         '[tablecloth.api :as tablecloth]
         '[tech.v3.datatype :as dt]
         '[tech.v3.tensor :as dtt]
         '[tech.viz.vega :as viz]
         '[aerial.hanami.common :as hanami-common]
         '[aerial.hanami.templates :as hanami-templates]
         '[scicloj.data :as data]
         '[scicloj.helpers :as helpers]
         '[scicloj.helpers.vega :as helpers.vega]
         '[tech.v3.libs.smile.regression]
         '[tech.v3.ml :as ml]
         '[tech.v3.dataset.modelling :as ds-mod]
         '[tech.v3.dataset.tensor :as dst]
         '[tech.v3.dataset :as ds]
         '[tech.v3.libs.smile.discrete-nb :as ml-nb]
         '[tech.v3.libs.smile.nlp :as ml-nlp]
         '[tech.v3.ml.classification :as ml-class]
         '[tech.v3.ml.metrics :as ml-metrics]
         '[scicloj.helpers.datasets :as datasets]
         '[fastmath.random :as fm.rand]
         '[tech.v3.datatype.functional :as dtype-fn ]
         '[tech.v3.datatype.functional :refer [* + - dot-product sin]]
         '[scicloj.helpers.sklearn :as sklearn]
         )

(import '[smile.stat.distribution Distribution GaussianDistribution]
        [smile.classification NaiveBayes])


["# Simple Linear Regression"]
["
```python
rng = np.random.RandomState(1)
x = 10 * rng.rand(50)
y = 2 * x - 5 + rng.randn(50)
plt.scatter(x, y);
```
"]

(defn t
  ([x]
   (let [seq (if (seq? x) x [x] ) ]
     (dtt/->tensor seq :datatype :float32)))
  ([x shape]
   (let [seq (if (seq? x) x [x] ) ]
     (dtt/broadcast (dtt/->tensor seq :datatype :float32) shape))))

(defn frand-t [s]
(-> (dtt/compute-tensor s (fn [& _] (fm.rand/frand)) :float32)
         (dtt/->tensor)))

(defn grand-t [s]
  (-> (dtt/compute-tensor s (fn [& _] (fm.rand/grand)) :float32)
      (dtt/->tensor)))

(def x
  (* (t 10 [50])
     (frand-t [50])))

(def y
  (+
   (-
    (* (t 2 [50]) x)
    (t 5 [50]))
   (grand-t [50])))

(def x-y-data
  (map
   (fn [x y] {:x x :y y})
   x y))


^kind/vega
{:data {:values x-y-data}
 :mark {:type  "point" :filled true :opacity 1}
 :encoding
 {:x
  {:field :x :type "quantitative"}
  :y
  {:field :y
   :type "quantitative"}}}


["```
from sklearn.linear_model import LinearRegression
model = LinearRegression(fit_intercept=True)

model.fit(x[:, np.newaxis], y)

xfit = np.linspace(0, 10, 1000)
yfit = model.predict(xfit[:, np.newaxis])

plt.scatter(x, y)
plt.plot(xfit, yfit);



```
" ]


(def ds
  (->
   (tablecloth/dataset x-y-data)
   (ds-mod/set-inference-target :y))
  )

(def trained-ols
  (tech.v3.ml/train
   ds
   {:model-type :smile.regression/ordinary-least-square}))

(def x-fit (-> (tablecloth/dataset  (map #(hash-map :x %) (range 0 10 0.01)))))
(def y-fit (ml/predict x-fit trained-ols))


(def regression-line-data
    (map
     #(hash-map :x %1 :y %2)
     (:x x-fit)
     (:y y-fit)))

^kind/vega
{:width 500
 :height 500
 :layer [{:data
          {:values x-y-data}
          :mark {:type "point" :filled true :opacity 1 }
          :encoding
          {:x
           {:field :x :type "quantitative"
            :scale {:domain [-2 12]}}
           :y
           {:field :y
            :type "quantitative"
            :scale {:domain [-10 20]}}}
          }


         {
          :data
          {:values regression-line-data}
          :mark {:type "point" :filled true :opacity 1 :size 1}
          :encoding
          {:x
           {:field :x :type "quantitative"}
           :y
           {:field :y
            :type "quantitative"}}}]

 }



(def thaw-fn
  (ml/options->model-def (:options trained-ols)))

(def ols
  (ml/thaw-model trained-ols thaw-fn))

(let [[intercept slope]
      (seq (.coefficients ols))]
  {:intercept intercept
   :slope slope}
  )

(str/split
 (str ols)
#"\n"
 )

["
```python
rng = np.random.RandomState(1)
X = 10 * rng.rand(100, 3)
y = 0.5 + np.dot(X, [1.5, -2., 1.])

model.fit(X, y)
print(model.intercept_)
print(model.coef_)
```

"]


(def X
  (* (t 10 [100 3])
     (frand-t [100 3])))


(def y
  (dtt/compute-tensor [100] (fn [i]
                              (+ 0.5 (dot-product
                                      (dtt/select X i :all)
                                      (dtt/ensure-tensor [1.5 -2  1])

                                      ))
                              ) :float32))

(def X-y-ds
  (-> X
      (tech.v3.dataset.tensor/tensor->dataset)
      (tablecloth/add-or-replace-column :y  y)
      (ds-mod/set-inference-target :y)
      ))

(def trained-ols
  (tech.v3.ml/train
   X-y-ds
   {:model-type :smile.regression/ordinary-least-square}))

(seq (.coefficients (ml/thaw-model trained-ols thaw-fn)))


["## Polynomial basis functions"]

["
```python
from sklearn.preprocessing import PolynomialFeatures
x = np.array([2, 3, 4])
poly = PolynomialFeatures(3, include_bias=False)
poly.fit_transform(x[:, None])
```"]

(def x [2 3 4])

(->
  (tablecloth/dataset {:x x})
  (sklearn/fit-transform :preprocessing :polynomial-features  {:degree 3 :include_bias false})
  )



["
```python
from sklearn.pipeline import make_pipeline
poly_model = make_pipeline(PolynomialFeatures(7),
                           LinearRegression())
```"]


(defn pipeline [ds]
  (-> ds
      (sklearn/fit-transform :preprocessing :polynomial-features  {:degree 7}))
  )



["
```python
rng = np.random.RandomState(1)
x = 10 * rng.rand(50)
y = np.sin(x) + 0.1 * rng.randn(50)

poly_model.fit(x[:, np.newaxis], y)
yfit = poly_model.predict(xfit[:, np.newaxis])

plt.scatter(x, y)
plt.plot(xfit, yfit);
```"]



(def x  (* (t 10 [50])  (frand-t [50])))
(def y   (+   (sin x) (* (t 0.1 [50]) (grand-t [50]))))
(def x-y-ds
  (-> {:x x :y y}
      (tablecloth/dataset)
      (ds-mod/set-inference-target :y)
      ))


(def trained-ols
  (tech.v3.ml/train (pipeline x-y-ds)
   {:model-type :smile.regression/ordinary-least-square}))

(def y-fit (ml/predict (pipeline x-fit) trained-ols))


(def regression-curve-data
  (map
   #(hash-map :x %1 :y %2)
   (:x x-fit)
   (:y y-fit)))



^kind/vega
{:width 500
 :height 500
 :layer [{:data
          {:values (tablecloth/rows x-y-ds :as-maps)}
          :mark {:type "point" :filled true :opacity 1 }
          :encoding
          {:x
           {:field :x :type "quantitative"}
           :y
           {:field :y
            :type "quantitative"
            }}
          }

         {
          :data
          {:values regression-curve-data}
          :mark {:type "point" :filled true :opacity 1 :size 1}
          :encoding
          {:x
           {:field :x :type "quantitative"}
           :y
           {:field :y
            :type "quantitative"}}}]

 }

["## Gaussian basis functions"]
