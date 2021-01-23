(ns scicloj.05-05-naive-bayes
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

["# In Depth: Naive Bayes Classification"]


(require '[clojure.java.io :as io]
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
         '[tech.v3.libs.smile.classification]
         '[tech.v3.ml :as ml]
         '[tech.v3.dataset.modelling :as ds-mod]
         '[tech.v3.libs.smile.discrete-nb :as ml-nb]
         '[tech.v3.libs.smile.nlp :as ml-nlp]
         '[tech.v3.ml.classification :as ml-class]
         '[tech.v3.ml.metrics :as ml-metrics]
         '[scicloj.helpers.datasets :as datasets]
         '[fastmath.random :as random]
         '[tech.v3.datatype.functional :as dtype-fn]

         )
(import '[smile.stat.distribution Distribution GaussianDistribution]
        [smile.classification NaiveBayes])

["## Bayesian Classification"]
["TODO"]

["## Gaussian Naive Bayes"]

["
```python
from sklearn.datasets import make_blobs
X, y = make_blobs(100, 2, centers=2, random_state=2, cluster_std=1.5)
plt.scatter(X[:, 0], X[:, 1], c=y, s=50, cmap='RdBu');
```
"]



(def blobs
  (-> (random/rng :isaac 1337)
      ;; (random/rng :isaac 928)
      (datasets/make-blob 100 2 1.5))
  )

(def max-x (+ 2 (apply dtype-fn/max (blobs :x))))
(def max-y (+ 2  (apply dtype-fn/max (blobs :y))))
(def min-x (- (apply dtype-fn/min (blobs :x))  2) )
(def min-y (-  (apply dtype-fn/min (blobs :y)) 2) )
(def range-x  (- max-x min-x))
(def range-y  (- max-y min-y))

^kind/vega
(-> blobs
 (tablecloth/rows :as-maps)
 (viz/scatterplot
  :x :y
  {:label-key :i}))

(defn get-x-i [ds i x-or-y]
  (-> ds
      (tablecloth/select-rows (comp #(= i %) :i ) )
      (tablecloth/select-columns x-or-y)
      (get x-or-y)
      (dt/->double-array)
      ))

(def cond-prob
  (into-array [(into-array Distribution [(GaussianDistribution/fit (get-x-i blobs 0 :x ))
                                         (GaussianDistribution/fit (get-x-i blobs 0 :y))]

                           )
               (into-array Distribution [(GaussianDistribution/fit (get-x-i blobs 1 :x ))
                                         (GaussianDistribution/fit (get-x-i blobs 1 :y ))])]))

(def nb
  (NaiveBayes. (double-array [0.5 0.5]) cond-prob ))

(def x-new
  (->>
   (take 2000
         (random/sequence-generator :default 2))
    (map
     #(fastmath.vector/emult [range-x range-y] %))
    (map
     #(fastmath.vector/add [  min-x  min-y ] %))))

(def y-new
  (map
   #(.predict nb (dt/->double-array %))
   x-new))

(def gaussian-class
  (map
   (fn [y [x1 x2]]
     (hash-map
      :x x1
      :y x2
      :i (+  y 2)))
   y-new
   x-new))

(def blobs+gauss
  (concat (tablecloth/rows  blobs :as-maps)
          gaussian-class))

^kind/vega
{:$schema
 "https://vega.github.io/schema/vega-lite/v4.json",
 :width 600
 :height 400
 :data {:values blobs+gauss}
 :layer [
         {
          :transform [{:filter {"field" "i", "gt" 1}}]
          :encoding
          {:x {:field "x", :type "quantitative"  :scale {:domain [min-x max-x]}},
           :y {:field "y", :type "quantitative"  :scale {:domain [min-y max-y]}},
           :color
           {:field "i" :type "nominal"
            :scale {:range ["red" "blue"] }
            }
           }
          :mark {:type "circle" :fillOpacity 0.2}}
         {
          :transform [{:filter {"field" "i", "lt" 2}}]
          :encoding
          {:x {:field "x", :type "quantitative" :scale {:domain [min-x max-x]} },
           :y {:field "y", :type "quantitative" :scale {:domain [min-y max-y]} },
           :color
           {:field "i" :type "nominal"
            :scale {:range ["red" "blue"] }
            :legend nil
            }
           }
          :mark {:type  "circle"
                 :fillOpacity 1
                 :size 50}
          }
         ]
 :config
 {:axis {:grid true}}}



^kind/vega
;; (-> gaussian-class
;;  ;; (tablecloth/rows :as-maps)
;;  (viz/scatterplot
;;   :x1 :x2
;;   {:label-key :y}))


["TODO

```python
from sklearn.datasets import make_blobs
X, y = make_blobs(100, 2, centers=2, random_state=2, cluster_std=1.5)

fig, ax = plt.subplots()

ax.scatter(X[:, 0], X[:, 1], c=y, s=50, cmap='RdBu')
ax.set_title('Naive Bayes Model', size=14)

xlim = (-8, 8)
ylim = (-15, 5)

xg = np.linspace(xlim[0], xlim[1], 60)
yg = np.linspace(ylim[0], ylim[1], 40)
xx, yy = np.meshgrid(xg, yg)
Xgrid = np.vstack([xx.ravel(), yy.ravel()]).T

for label, color in enumerate(['red', 'blue']):
    mask = (y == label)
    mu, std = X[mask].mean(0), X[mask].std(0)
    P = np.exp(-0.5 * (Xgrid - mu) ** 2 / std ** 2).prod(1)
    Pm = np.ma.masked_array(P, P < 0.03)
    ax.pcolorfast(xg, yg, Pm.reshape(xx.shape), alpha=0.5,
                  cmap=color.title() + 's')
    ax.contour(xx, yy, P.reshape(xx.shape),
               levels=[0.01, 0.1, 0.5, 0.9],
               colors=color, alpha=0.2)

ax.set(xlim=xlim, ylim=ylim)

fig.savefig('figures/05.05-gaussian-NB.png')
```
"]
["TODO"]
["## Multinomial Naive Bayes"]
["### Example: Classifying Text"]
["
 ```python
from sklearn.datasets import fetch_20newsgroups

data = fetch_20newsgroups()
data.target_names
```
"
 ]

["Load the newsgroup 20 data and split in test and train."]
(defn data->ds [train-or-test]
  (let [data
        (->> (file-seq (io/file (str  "resources/data/20news/20news-bydate-" (name train-or-test))))
             (filter #(.isFile %))
             (map  #(hash-map
                     :text (slurp (.getPath %))
                     :category (.getName (.getParentFile %))))


             )]
    (tablecloth/dataset {:text (map :text data )
                         :category (map :category data)})
    ))

(def data (data/news-group-20-ds))

(def train (data :train))
(def test (data :test))


(distinct (:category train))


["One row of data looks like this"]
^kind/dataset-grid
(tablecloth/rand-nth train)

^kind/md
(first (:text train))

["```python
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.pipeline import make_pipeline

model = make_pipeline(TfidfVectorizer(), MultinomialNB())
```"]


["We first need to create a common vocabulary for test and train."]
(def common-vocab
  (->
   (tablecloth/concat
    train
    test)
   (ml-nlp/count-vectorize :text :bow ml-nlp/default-text->bow)
   :bow
   (ml-nlp/->vocabulary-top-n 10000)))

["This is the pipeline definition. Using the discrete-naive-bayes classifier,
we don't use a tfidf transformation step in the pipeline.
The model itself hasn an option for this."
 ]

["We first tokenize the text and then transform the tokens into a indexed, sparse format
using the vocabulary defined before"]

(defn filter-cats [ds]
  (tablecloth/select-rows
   ds
   (comp #(contains? #{"talk.religion.misc"
                       "soc.religion.christian"
                       "sci.space"
                       "comp.graphics"}
                     %) :category))
  )

(defn run-pipeline [ds]
  (-> ds
      (ml-nlp/count-vectorize :text :bow ml-nlp/default-text->bow)
      (ml-nb/bow->SparseArray :bow :sparse (fn [ignore-me] common-vocab) )
      (dataset/categorical->number cf/categorical {} :int32)
      (ds-mod/set-inference-target :category)))

["Creating the preprocessed datasets"]
(def train (-> train filter-cats run-pipeline))
(def test (-> test filter-cats run-pipeline) )



["```python
model.fit(train.data, train.target)
labels = model.predict(test.data)
```"]


["Train the naive bayes model"]
(def trained-model
  (ml/train train  {:model-type :discrete-naive-bayes
                    :sparse-column :sparse
                    :discrete-naive-bayes-model :twcnb ;; variation of tfidf
                    :k (count (distinct (train :category)))}))



["Make prediction and"]

(def prediction
  (-> (ml/predict test trained-model)))

["evaluate against test set:"]
(ml-metrics/accuracy  (:category test) (:category prediction))


["```python
from sklearn.metrics import confusion_matrix
mat = confusion_matrix(test.target, labels)
sns.heatmap(mat.T, square=True, annot=True, fmt='d', cbar=False,
            xticklabels=train.target_names, yticklabels=train.target_names)
plt.xlabel('true label')
plt.ylabel('predicted label');
```"]

["Prepare confusion matrix:"]
(def index->cat-map
  (-> train
      ds-cat/dataset->categorical-maps
      second
      :lookup-table
      (clojure.set/map-invert)))

(def predicted-categories
  (map  index->cat-map (:category prediction) ))

(def true-categories
  (map
   #(index->cat-map (int %))
   (:category test) ))



(def cm-ds
  (->
   (ml-class/confusion-map  predicted-categories true-categories :none)
   (ml-class/confusion-map->ds :none)))

^kind/dataset
cm-ds

(def cm-ds-long
  (flatten
   (map
    (fn [[x ys] ]
      (map
       #(hash-map :x %1
                  :y %2
                  :c %3)
       (repeat (count ys) x)
       (map key ys)
       (map val ys)))
    (ml-class/confusion-map  predicted-categories true-categories :none))))


^kind/vega
{:$schema
 "https://vega.github.io/schema/vega-lite/v4.json",
 :width 300
 :height 300
 :data {:values cm-ds-long},
 ,
 :encoding
 {:y {:field "x", :type "nominal"},
  :x {:field "y", :type "nominal"},
  :color
  {:field "c" :type "quantitative"
   :scale {:scheme "yelloworangered"}}
  }
 :layer [
         {:mark "rect"
           }
         {:mark "text"
          :encoding {
                     :text {:field "c" :type "ordinal"}

                     :color {:value "black"}}
          }
         ]
 :config
 {:axis {:grid true, :tickBand "extent"}}}


["```python
def predict_category(s, train=train, model=model):
    pred = model.predict([s])
    return train.target_names[pred[0]]
predict_category('sending a payload to the ISS')

```"]


(defn predict-category [text]
  (->> trained-model
       (ml/predict
        (run-pipeline
         (tablecloth/dataset {:text [ text]
                              :category [nil]})))
       :category
       (map index->cat-map)
       first))


(predict-category "sending a payload to the ISS")

(predict-category "discussing islam vs atheism'")

(predict-category "determining the screen resolution")
