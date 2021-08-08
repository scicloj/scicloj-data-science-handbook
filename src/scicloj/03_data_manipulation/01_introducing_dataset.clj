(ns scicloj.03-data-manipulation.01-introducing-dataset
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]
            [clojure.java.io :as io]
            [tablecloth.api :as tablecloth]))

;; Notespace
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
  ;; Generate static site
  (notespace/render-static-html))

["# Introducing Tablecloth Objects"]

["At the very basic level, Tablecloth dataset is using tech.ml.dataset as the
base. It is in-memory columnwise database. As we will see during the course of
this chapter, Tablecloth provides a host of useful tools, methods, and
functionality on top of the basic data structures, but nearly everything that
follows will require an understanding of what these structures are. Thus, before
we go any further, let's introduce these two fundamental Tablecloth data
structures: the Column and Dataset.

We will start our code sessions with the Tablecloth require:"]

(require '[tablecloth.api :as tablecloth])

;; TODO: as there is no Series object in tablecloth, we will skip Series Object section
;; To start with Dataset object

["## Constructing Dataset objects"]

["The fundamental structure in tablecloth is the Dataset. The Dataset can be
thought of either as a generalization of a NumPy array, or as a specialisation
of a Clojure dictionary. We'll now take a look at each of these perspectives.

A Dataset can be constructed in a variety of ways. Here we'll give
several examples."]

["### Dataset as a generalized Clojure vector"]

["If a clojure vector is an analog of a one-dimensional array with flexible
indices, a Dataset is an analog of a two-dimensional vector with both flexible
row indices and flexible column names. Just as you might think of a
two-dimensional vector as an ordered sequence of aligned one-dimensional
columns, you can think of a Dataset as a sequence of aligned Column
objects. Here, by \"aligned\" we mean that they share the same index.

To demonstrate this, let's first construct a new Dataset listing the area of
each of the five states:"]

(def names ["California" "Texas" "New York" "Florida" "Illinois"])
(def area [423967 695662 141297 170312 149995])
(def area-map {:name names
               :area area})

^kind/dataset
(tablecloth/dataset area-map)
;; => _unnamed [5 2]:
;;    |      :name |  :area |
;;    |------------|--------|
;;    | California | 423967 |
;;    |      Texas | 695662 |
;;    |   New York | 141297 |
;;    |    Florida | 170312 |
;;    |   Illinois | 149995 |

["Now that we have this along with the population Column map from before, we can
use a map to construct a single two-dimensional object containing this
information:"]

(def population [38332521 19552860 12882135 19651127 26448193])

(def states (tablecloth/dataset {:name names
                                 :area area
                                 :population population}))
^kind/dataset
states
;; => _unnamed [5 3]:
;;    |      :name |  :area | :population |
;;    |------------|-------:|------------:|
;;    | California | 423967 |    38332521 |
;;    |      Texas | 695662 |    19552860 |
;;    |   New York | 141297 |    12882135 |
;;    |    Florida | 170312 |    19651127 |
;;    |   Illinois | 149995 |    26448193 |

["The Dataset row is indexed by numbers:"]

^kind/dataset
(tablecloth/select-rows states 0)
;; => _unnamed [1 3]:
;;    |      :name |  :area | :population |
;;    |------------|-------:|------------:|
;;    | California | 423967 |    38332521 |

^kind/dataset
(tablecloth/select-rows states [0 1 2])
;; => _unnamed [3 3]:
;;    |      :name |  :area | :population |
;;    |------------|-------:|------------:|
;;    | California | 423967 |    38332521 |
;;    |      Texas | 695662 |    19552860 |
;;    |   New York | 141297 |    12882135 |

["Additionally, the Dataset has a columns attribute, which is an Index object
holding the column labels:"]

(tablecloth/column-names states)
;; => (:name :area :population)

["Thus the Dataset can be thought of as a generalization of a two-dimensional
clojure vector, where the columns have a generalised index, and the rows has a
numeric index for accessing the data."]

["### Dataset as specialised map"]

["Similarly, we can also think of a Dataset as a specialization of a map. Where
a map maps a key to a value, a Dataset maps a column name to a column data. For
example, asking for the 'area' attribute returns the sub-dataset object
containing the areas we saw earlier:"]

^kind/dataset
(tablecloth/select-columns states :area)
;; => _unnamed [5 1]:
;;    |  :area |
;;    |-------:|
;;    | 423967 |
;;    | 695662 |
;;    | 141297 |
;;    | 170312 |
;;    | 149995 |

["Notice the potential point of confusion here: in a two-dimesnional clojure
vector, (data 0) will return the first row. For a tablecloth dataset,
(data col0) return the first column. Because of this, it is probably better to
think about Datasets as generalized maps rather than generalized vectors, though
both ways of looking at the situation can be useful. We'll explore more flexible
means of indexing Datasets in Data Indexing and Selection."]

["### Constructing Dataset objects"]

["A tablecloth Dataset can be constructed in a variety of ways. Here we'll give
several examples."]

["#### From a single value"]

^kind/dataset
(tablecloth/dataset 99)
;; => _unnamed [1 1]:
;;    | :$value |
;;    |--------:|
;;    |      99 |

["or you can assign a name to the dataset and a name to the column:"]

^kind/dataset
(tablecloth/dataset 99 {:single-value-column-name :single-value :dataset-name "Single Value"})
;; => Single Value [1 1]:
;;    | :single-value |
;;    |--------------:|
;;    |            99 |

["or you can combine the column name and value together in a map pair:"]

^kind/dataset
(tablecloth/dataset {:single-value 99}
                    {:dataset-name "Single Value"})
;; => Single Value [1 1]:
;;    | :single-value |
;;    |--------------:|
;;    |            99 |


["#### From a two dimensional vector"]

^kind/dataset
(tablecloth/dataset [[1 2][3 4][4 5]])
;; => :_unnamed [3 2]:
;;    | 0 | 1 |
;;    |--:|--:|
;;    | 1 | 2 |
;;    | 3 | 4 |
;;    | 4 | 5 |

["The default layout is :as-rows, we can also transpose it :as-columns"]

^kind/dataset
(tablecloth/dataset [[1 2][3 4][4 5]]
                    {:layout :as-columns
                     :column-names [:a :b :c]
                     :dataset-name "Three columns"})
;; => Three columns [2 3]:
;;    | :a | :b | :c |
;;    |---:|---:|---:|
;;    |  1 |  3 |  4 |
;;    |  2 |  4 |  5 |


["#### From a map of column name and values seq"]

["A Dataset is a collection of Column objects, and a single-column Dataset can
be constructed from a map of column name and values seq:"]

^kind/dataset
(tablecloth/dataset {:population population})
;; => _unnamed [5 1]:
;;    | :population |
;;    |-------------|
;;    |    38332521 |
;;    |    19552860 |
;;    |    12882135 |
;;    |    19651127 |
;;    |    26448193 |

["#### From a sequence of map"]

["Any seq of maps can be made into a Dataset. We'll use a simple `map` to create
some data:"]

(def data (map (fn [i] {:row-id i :a i :b (* 2 i)}) (range 3)))
data
;; => ({:row-id 0, :a 0, :b 0} {:row-id 1, :a 1, :b 2} {:row-id 2, :a 2, :b 4})

^kind/dataset
(tablecloth/dataset data)
;; => _unnamed [3 3]:
;;    | :row-id | :a | :b |
;;    |---------|----|----|
;;    |       0 |  0 |  0 |
;;    |       1 |  1 |  2 |
;;    |       2 |  2 |  4 |

["Even if some keys in the map are missing, tablecloth will fill them in with
nil values:"]

^kind/dataset
(tablecloth/dataset [{:a 1 :b 2} {:b 3 :c 4}])
;; => _unnamed [2 3]:
;;    | :a | :b | :c |
;;    |----|----|----|
;;    |  1 |  2 |    |
;;    |    |  3 |  4 |


;; TODO: bug? if we add another :d column, the column order is shuffled.
^kind/dataset
(tablecloth/dataset [{:a 1 :b 2} {:b 3 :c 4} {:d 99}])
;; => _unnamed [3 4]:
;;    | :d | :b | :c | :a |
;;    |---:|---:|---:|---:|
;;    |    |  2 |    |  1 |
;;    |    |  3 |  4 |    |
;;    | 99 |    |    |    |

["#### From a map of sequence"]

["As we saw before, a Dataset can be constructed from a map of seq objects as
well:"]

^kind/dataset
(tablecloth/dataset {:name names
                     :population population
                     :area area})
;; => _unnamed [5 3]:
;;    |      :name | :population |  :area |
;;    |------------|-------------|--------|
;;    | California |    38332521 | 423967 |
;;    |      Texas |    19552860 | 695662 |
;;    |   New York |    12882135 | 141297 |
;;    |    Florida |    19651127 | 170312 |
;;    |   Illinois |    26448193 | 149995 |

["or sequence of pairs:"]

^kind/dataset
(tablecloth/dataset [[:name names]
                     [:population population]
                     [:area area]])
;; => _unnamed [5 3]:
;;    |      :name | :population |  :area |
;;    |------------|-------------|--------|
;;    | California |    38332521 | 423967 |
;;    |      Texas |    19552860 | 695662 |
;;    |   New York |    12882135 | 141297 |
;;    |    Florida |    19651127 | 170312 |
;;    |   Illinois |    26448193 | 149995 |

["#### From a csv file"]

^kind/dataset
(tablecloth/dataset "resources/data/iris.csv")
;; => resources/data/iris.csv [150 5]:
;;    | Sepal.Length | Sepal.Width | Petal.Length | Petal.Width | Species |
;;    |-------------:|------------:|-------------:|------------:|---------|
;;    |          5.1 |         3.5 |          1.4 |         0.2 |  setosa |
;;    |          4.9 |         3.0 |          1.4 |         0.2 |  setosa |
;;    |          4.7 |         3.2 |          1.3 |         0.2 |  setosa |
;;    |          4.6 |         3.1 |          1.5 |         0.2 |  setosa |
;;    |          5.0 |         3.6 |          1.4 |         0.2 |  setosa |
;;    |          5.4 |         3.9 |          1.7 |         0.4 |  setosa |
;;    |          4.6 |         3.4 |          1.4 |         0.3 |  setosa |
;;    |          5.0 |         3.4 |          1.5 |         0.2 |  setosa |
;;    |          4.4 |         2.9 |          1.4 |         0.2 |  setosa |
;;    |          4.9 |         3.1 |          1.5 |         0.1 |  setosa |
;;    |          5.4 |         3.7 |          1.5 |         0.2 |  setosa |
;;    |          4.8 |         3.4 |          1.6 |         0.2 |  setosa |
;;    |          4.8 |         3.0 |          1.4 |         0.1 |  setosa |
;;    |          4.3 |         3.0 |          1.1 |         0.1 |  setosa |
;;    |          5.8 |         4.0 |          1.2 |         0.2 |  setosa |
;;    |          5.7 |         4.4 |          1.5 |         0.4 |  setosa |
;;    |          5.4 |         3.9 |          1.3 |         0.4 |  setosa |
;;    |          5.1 |         3.5 |          1.4 |         0.3 |  setosa |
;;    |          5.7 |         3.8 |          1.7 |         0.3 |  setosa |
;;    |          5.1 |         3.8 |          1.5 |         0.3 |  setosa |
;;    |          5.4 |         3.4 |          1.7 |         0.2 |  setosa |
;;    |          5.1 |         3.7 |          1.5 |         0.4 |  setosa |
;;    |          4.6 |         3.6 |          1.0 |         0.2 |  setosa |
;;    |          5.1 |         3.3 |          1.7 |         0.5 |  setosa |
;;    |          4.8 |         3.4 |          1.9 |         0.2 |  setosa |

["or from a url:"]

^kind/dataset
(tablecloth/dataset
 "https://raw.githubusercontent.com/scicloj/scicloj-data-science-handbook/live/resources/data/iris.csv")
;; => https://raw.githubusercontent.com/scicloj/scicloj-data-science-handbook/live/resources/data/iris.csv [150 5]:
;;    | Sepal.Length | Sepal.Width | Petal.Length | Petal.Width | Species |
;;    |-------------:|------------:|-------------:|------------:|---------|
;;    |          5.1 |         3.5 |          1.4 |         0.2 |  setosa |
;;    |          4.9 |         3.0 |          1.4 |         0.2 |  setosa |
;;    |          4.7 |         3.2 |          1.3 |         0.2 |  setosa |
;;    |          4.6 |         3.1 |          1.5 |         0.2 |  setosa |
;;    |          5.0 |         3.6 |          1.4 |         0.2 |  setosa |
;;    |          5.4 |         3.9 |          1.7 |         0.4 |  setosa |
;;    |          4.6 |         3.4 |          1.4 |         0.3 |  setosa |
;;    |          5.0 |         3.4 |          1.5 |         0.2 |  setosa |
;;    |          4.4 |         2.9 |          1.4 |         0.2 |  setosa |
;;    |          4.9 |         3.1 |          1.5 |         0.1 |  setosa |
;;    |          5.4 |         3.7 |          1.5 |         0.2 |  setosa |
;;    |          4.8 |         3.4 |          1.6 |         0.2 |  setosa |
;;    |          4.8 |         3.0 |          1.4 |         0.1 |  setosa |
;;    |          4.3 |         3.0 |          1.1 |         0.1 |  setosa |
;;    |          5.8 |         4.0 |          1.2 |         0.2 |  setosa |
;;    |          5.7 |         4.4 |          1.5 |         0.4 |  setosa |
;;    |          5.4 |         3.9 |          1.3 |         0.4 |  setosa |
;;    |          5.1 |         3.5 |          1.4 |         0.3 |  setosa |
;;    |          5.7 |         3.8 |          1.7 |         0.3 |  setosa |
;;    |          5.1 |         3.8 |          1.5 |         0.3 |  setosa |
;;    |          5.4 |         3.4 |          1.7 |         0.2 |  setosa |
;;    |          5.1 |         3.7 |          1.5 |         0.4 |  setosa |
;;    |          4.6 |         3.6 |          1.0 |         0.2 |  setosa |
;;    |          5.1 |         3.3 |          1.7 |         0.5 |  setosa |
;;    |          4.8 |         3.4 |          1.9 |         0.2 |  setosa |
