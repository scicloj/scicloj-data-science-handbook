(ns scicloj.03-data-manipulation-with-tablecloth
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
  (notespace/eval-this-notespace))

;; Chapter 03 - Data Manipulation with tablecloth
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;

["# Chapter 03 - Data Manipulation with tablecloth"]

["In the previous chapter, we dove into detail on (...)

In this chapter, we will focus on the mechanics of using datasets (?Series,
DataFrame?), and related structures effectively. We will use examples drawn from
real datasets where appropriate, but these examples are not necessarily the
focus."]

["## Installing and Using tablecloth"

 "Installation of tablecloth is just as easy as add it to your deps.edn:
  :deps
  {
    scicloj/tablecloth {:mvn/version \"5.00-beta-5a\"}
  }"]

["Once tablecloth is added to your project deps, you can require it and check the version:"]

(require '[tablecloth.api :as tablecloth])

;; TODO: we cannot check verson of tablecloth now. which is easy in pandas: `pandas.__version__`

;; TODO we need to define a shorter alias for tablecloth later.

["## Introducing Tablecloth Objects"]

["At the very basic level, Tablecloth dataset is using tech.ml.dataset as the
base. It is in-memory columnwise database. As we will see during the course of
this chapter, Tablecloth provides a host of useful tools, methods, and
functionality on top of the basic data structures, but nearly everything that
follows will require an understanding of what these structures are. Thus, before
we go any further, let's introduce these two fundamental Tablecloth data
structures: the Column and Dataset.

We will start our code sessions with the Tablecloth require:"]

(require '[tech.v3.dataset.column :as col])

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

(def state-names [:California :Texas :New-York :Florida :Illinois])
state-names
;; => [:California :Texas :New-York :Florida :Illinois]

(def area [423967
           695662
           141297
           170312
           149995])

(def area-ds (tablecloth/dataset {:area area}))
^kind/dataset
area-ds
;; => _unnamed [5 1]:
;;    |  :area |
;;    |--------|
;;    | 423967 |
;;    | 695662 |
;;    | 141297 |
;;    | 170312 |
;;    | 149995 |

(def population [38332521
                 26448193
                 19651127
                 19552860
                 12882135])

(def population-ds (tablecloth/dataset {:population population}))
^kind/dataset
population-ds
;; => _unnamed [5 1]:
;;    | :population |
;;    |-------------|
;;    |    38332521 |
;;    |    26448193 |
;;    |    19651127 |
;;    |    19552860 |
;;    |    12882135 |

(def states (tablecloth/dataset {:name state-names
                                 :area area
                                 :population population}))
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

["#### From a seq of map"]

["Any seq of maps can be made into a Dataset. We'll use a simple `map` to create some data:"]

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

["From a map of seq objects"]

["As we saw before, a Dataset can be constructed from a map of seq objects as
well:"]

^kind/dataset
(tablecloth/dataset {:name state-names
                     :population population
                     :area area})
;; => _unnamed [5 3]:
;;    |       :name | :population |  :area |
;;    |-------------|-------------|--------|
;;    | :California |    38332521 | 423967 |
;;    |      :Texas |    26448193 | 695662 |
;;    |   :New-York |    19651127 | 141297 |
;;    |    :Florida |    19552860 | 170312 |
;;    |   :Illinois |    12882135 | 149995 |


["## Data Indexing and Selection"]

["### Data Selection in Dataset"]

["#### Dataset as map"]

(def data (tablecloth/dataset (zipmap [:a :b :c :d] [0.25 0.5 0.75 1.0])))

^kind/dataset
data

["We can use dataset as function to select column:"]

(data :b)

["Or use keyword as function:"]

(:b data)

["Check whether there is a specific column with column name:"]

(tablecloth/has-column? data :b)

["We can also get all the column names with `column-names`"]

(tablecloth/column-names data)

["List all the columns"]

(tablecloth/columns data)

["We can add or update column:"]

["update existing column"]

(tablecloth/add-or-replace-column data :d 1.1)

["add new column"]
(tablecloth/add-or-replace-column data :e 1.25)


["## The tablecloth dataset (Series?) Object

A tablecloth dataset is a (...) It can be created from a map as follows:
"]

(def population-map {:California 38332521,
                     :Texas 26448193,
                     :New_York 19651127,
                     :Florida 19552860,
                     :Illinois 12882135})

(def population (tablecloth/dataset population-map
                                    {:dataset-name "Population"}))

^kind/dataset
population

["By default, (...). From here, typical dictionary-style item access can be performed:"]

(:California population)

["(this ^ is actually a column, not a number)"]

["Unlike a map (dictionary), though, the Series also supports array-style operations such as slicing (FIXME):"]

["(...)"]

(vals population)
(keys population)
