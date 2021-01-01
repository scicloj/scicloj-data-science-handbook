(ns scicloj.03-data-manipulation.01-introducing-dataset
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]
            [clojure.java.io :as io]))

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
thought of either as a generalization of a NumPy array, or as a specialization
of a Clojure dictionary. We'll now take a look at each of these perspectives.

A Dataset can be constructed in a variety of ways. Here we'll give
several examples."]

["### Dataset as a generalized NumPy array"]

["If a Series is an analog of a one-dimensional array with flexible indices, a
Dataset is an analog of a two-dimensional array with both flexible row indices
and flexible column names. Just as you might think of a two-dimensional array as
an ordered sequence of aligned one-dimensional columns, you can think of a
Dataset as a sequence of aligned Column objects. Here, by \"aligned\" we mean
that they share the same index.

To demonstrate this, let's first construct a new Dataset listing the area of
each of the five states discussed in the previous section:"]

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

["The Dataset row is indexed by numbers:"]
^kind/dataset
(tablecloth/select-rows states 0)

^kind/dataset
(tablecloth/select-rows states [0 1 2])

["Additionally, the Dataset has a columns attribute, which is an Index object
holding the column labels:"]

(tablecloth/column-names states)

["Thus the Dataset can be thought of as a generalization of a two-dimensional
NumPy array, where both the rows and columns have a generalized index for
accessing the data."]

["### Dataset as specialized map"]

["Similarly, we can also think of a Dataset as a specialization of a map. Where
a map maps a key to a value, a Dataset maps a column name to a column data. For
example, asking for the 'area' attribute returns the sub-dataset object
containing the areas we saw earlier:"]

^kind/dataset
(tablecloth/select-columns states :area)

["Notice the potential point of confusion here: in a two-dimesnional NumPy
array, data[0] will return the first row. For a DataFrame, data['col0'] will
return the first column. Because of this, it is probably better to think about
Datasets as generalized maps rather than generalized arrays, though both ways of
looking at the situation can be useful. We'll explore more flexible means of
indexing Datasets in Data Indexing and Selection."]

["### Constructing Dataset objects"]

["A tableclot Dataset can be constructed in a variety of ways. Here we'll give
several examples."]

["#### From a map of column name and values seq"]

["A Dataset is a collection of Column objects, and a single-column Dataset can
be constructed from a map of column namd and values seq:"]

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

^kind/dataset
(tablecloth/dataset data)
;; => _unnamed [3 3]:
;;    | :row-id | :a | :b |
;;    |---------|----|----|
;;    |       0 |  0 |  0 |
;;    |       1 |  1 |  2 |
;;    |       2 |  2 |  4 |

["Even if some keys in the map are missing, tablecloth will fill them in with
NaN values:"]

^kind/dataset
(tablecloth/dataset [{:a 1 :b 2} {:b 3 :c 4}])
;; => _unnamed [2 3]:
;;    | :a | :b | :c |
;;    |----|----|----|
;;    |  1 |  2 |    |
;;    |    |  3 |  4 |

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

;; TODO: Tablecloth do not support this way:
#_["#### From a two-dimensional NumPy array"]

#_["Given a two-dimensional array of data, we can create a DataFrame with any
specified column and index names. If omitted, an integer index will be used for
each:"]
