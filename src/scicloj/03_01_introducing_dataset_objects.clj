(ns scicloj.03-01-introducing-dataset-objects
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]
            [clojure.java.io :as io]))

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
    ;; generate static site
  (notespace/render-static-html "docs/scicloj/ch03/03_01_introducing_dataset_object.html"))


["## Introducing Tablecloth Objects"]

["At the very basic level, Tablecloth dataset is using tech.ml.dataset as the
base. It is in-memory columnwise database. As we will see during the course of
this chapter, Tablecloth provides a host of useful tools, methods, and
functionality on top of the basic data structures, but nearly everything that
follows will require an understanding of what these structures are. Thus, before
we go any further, let's introduce these two fundamental Tablecloth data
structures: the Column and Dataset.

We will start our code sessions with the Tablecloth require:"]

(require '[tech.v3.dataset.column :as col]
         '[tablecloth.api :as tablecloth])

["### The tech.ml.dataset Column Object"]

["A Column is a one-dimensional array of indexed data. It can be created from a
vec or array as follows:"]

(def column1 (col/new-column :a-column [0.25 0.5 0.75 1.0]))
;; TODO: no notespace kind for column object at the moment
column1
;; => #tech.v3.dataset.column<object>[4]
;;    :a-column
;;    [0.2500, 0.5000, 0.7500, 1.000, ]

(def column2 (col/new-column :a-column (float-array [0.25 0.5 0.75 1.0])))
column2
;; => #tech.v3.dataset.column<float32>[4]
;;    :a-column
;;    [0.2500, 0.5000, 0.7500, 1.000, ]

["As we see in the output, the Column wraps both a sequence of values, which we
can access with the values and index attributes:"]

(vec column1)
;; => [0.25 0.5 0.75 1.0]

(vec column2)
;; => [0.25 0.5 0.75 1.0]

["Column can be accessed by the index. Just as clojure vector, you can invoke
column with index as the argument:"]

(column1 1)
;; => 0.5

["You can also select rows by seq of indices:"]

(col/select column1 [1 2])
;; => #tech.v3.dataset.column<object>[2]
;;    :a-column
;;    [0.5000, 0.7500, ]

(col/select column1 (range 1 3))
;; => #tech.v3.dataset.column<object>[2]
;;    :a-column
;;    [0.5000, 0.7500, ]

["It is different to Pandas Series that the index of rows can only be numbers."]


["## The tech.ml Dataset Object"]

["The next fundamental structure in tech.ml is the Dataset. The Dataset can be
thought of columnar based map. We'll now take a look at each of these
perspectives."]

["`tech.ml.dataset` is a Clojure library for data processing and machine
learning. Datasets are currently in-memory columnwise databases and support
parsing from file or input-stream."]

["To demonstrate this, let's first construct a new Dataset listing the area and
population of each of the five states:"]

(def state-name [:California :Texas :New-York :Florida :Illinois])
(def state-area [423967 695662 141297 170312 149995])
(def state-population [38332521 26448193 19651127 19552860 12882135])

["Now we can use maps to construct a single two-dimensional object containing
this information:"]

(def states (tablecloth/dataset {:name state-name
                                 :area state-area
                                 :population state-population}))
^kind/dataset
states
;; => _unnamed [5 3]:
;;    |       :name |  :area | :population |
;;    |-------------|--------|-------------|
;;    | :California | 423967 |    38332521 |
;;    |      :Texas | 695662 |    26448193 |
;;    |   :New-York | 141297 |    19651127 |
;;    |    :Florida | 170312 |    19552860 |
;;    |   :Illinois | 149995 |    12882135 |

["You can get the column names with `column-names` API:"]

(tablecloth/column-names states)
;; => (:name :area :population)

["`rows` API puts each row as vector in a vector:"]

(tablecloth/rows states)
;; => [[:California 423967 38332521] [:Texas 695662 26448193] [:New-York 141297 19651127] [:Florida 170312 19552860] [:Illinois 149995 12882135]]

["`columns` API puts each column as vector in a vector:"]

(tablecloth/columns states)


["### Dataset as specialized map"]

["Similarly, we can also think of a Dataset as a specialization of a map, which
maps a key to a value, a Dataset maps a column name to a Column data. For
example, asking for the :area attribute returns the Column object containing the
areas we saw earlier:"]

(states :area)
;; => #tech.v3.dataset.column<int64>[5]
;;    :area
;;    [423967, 695662, 141297, 170312, 149995, ]

(:area states)
;; => #tech.v3.dataset.column<int64>[5]
;;    :area
;;    [423967, 695662, 141297, 170312, 149995, ]

["### Constructing Dataset objects"]

["A Dataset can be constructed in a variety of ways. Here we'll give
several examples."]

["#### From a sequence of map"]

["Any seq of maps can be made into a Dataset. We'll use a simple `map` to create
some data:"]

(def data (map (fn [i] {:a i :b (* 2 i)}) (range 3)))

^kind/dataset
(tablecloth/dataset data)
;; => _unnamed [3 2]:
;;    | :a | :b |
;;    |----|----|
;;    |  0 |  0 |
;;    |  1 |  2 |
;;    |  2 |  4 |

["Even if some keys in the map are missing, tablecloth will fill them in with
NaN values:"]

^kind/dataset
(tablecloth/dataset [{:a 1 :b 2} {:b 3 :c 4}])
;; => _unnamed [2 3]:
;;    | :a | :b | :c |
;;    |----|----|----|
;;    |  1 |  2 |    |
;;    |    |  3 |  4 |

["#### From a map of sequence objects"]

["As we saw before, a Dataset can be constructed from a map of seq objects as
well:"]

^kind/dataset
(tablecloth/dataset {:name state-name
                     :population state-population
                     :area state-area})
;; => _unnamed [5 3]:
;;    |       :name | :population |  :area |
;;    |-------------|-------------|--------|
;;    | :California |    38332521 | 423967 |
;;    |      :Texas |    26448193 | 695662 |
;;    |   :New-York |    19651127 | 141297 |
;;    |    :Florida |    19552860 | 170312 |
;;    |   :Illinois |    12882135 | 149995 |

["or sequence of pairs:"]

^kind/dataset
(tablecloth/dataset [[:name state-name]
                     [:population state-population]
                     [:area state-area]])
