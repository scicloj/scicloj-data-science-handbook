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

(def column2 (col/new-column :a-column (float-array [0.25 0.5 0.75 1.0])))
column2

["As we see in the output, the Column wraps both a sequence of values, which we
can access with the values and index attributes:"]

(vec column1)

(vec column2)

["Column can be accessed by the index. Just as clojure vector, you can invoke
column with index as the argument:"]

(column1 1)

["You can also select rows by seq of indices:"]

(col/select column1 [1 2])

(col/select column1 (range 1 3))



["## The tech.ml.dataset"]

(def data (tablecloth/dataset [0.25 0.5 0.75 1.0]))

^kind/dataset
data

["## Data Selection in Series"]

["### Series/Dataframe as dictionary"]

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


["### Series as one-dimensional array"]



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
