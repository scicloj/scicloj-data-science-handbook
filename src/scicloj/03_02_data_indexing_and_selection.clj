(ns scicloj.03-02-data-indexing-and-selection
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
  (notespace/render-static-html "docs/scicloj/ch03/03_02_data_indexing_and_selection.html"))

["In Chapter 2, we looked in detail at methods and tools to access, set, and
modify values in clojure vec and map. These included indexing (e.g., `(avec 2)`
`(amap :akey)` and `(:akey amap)` ) Here we'll look at similar means of
accessing and modifying values in Dataset objects. If you have used the clojure
patterns, the corresponding patterns in dataset will feel very familiar, though
there are a few quirks to be aware of."]

#_["# Data Selection in Series(skip this section)"]

["# Data Selection in Dataset"]

["Recall that a Dataset acts in many ways like a map of columns. These analogies
can be helpful to keep in mind as we explore data selection within this
structure."]

["## DataFrame as a map"]

["The first analogy we will consider is the Dataset as a map of related Column
objects. Let's return to our example of areas and populations of states:"]

(require '[tablecloth.api :as tablecloth])
(def state-name [:California :Texas :New-York :Florida :Illinois])
(def state-area [423967 695662 141297 170312 149995])
(def state-population [38332521 26448193 19651127 19552860 12882135])
(def states (tablecloth/dataset {:name state-name
                                 :area state-area
                                 :pop state-population}))
^kind/dataset
states

["The individual Column that make up the DataFrame can be accessed via map-style
indexing of the column name:"]

(states :area)
;; => #tech.v3.dataset.column<int64>[5]
;;    :area
;;    [423967, 695662, 141297, 170312, 149995, ]

["Equivalently, we can use keyword-style access with column names that are
keywords:"]

(:area states)
;; => #tech.v3.dataset.column<int64>[5]
;;    :area
;;    [423967, 695662, 141297, 170312, 149995, ]

["This keyword-style column access actually accesses the exact same object as
the map-style access:"]

(identical? (:area states) (states :area))
;; => true

["Though this is a useful shorthand, keep in mind that it does not work for all
cases! For example, if the column names are not keywords, this keyword-style
access is not possible."]

["Like with the Series objects discussed earlier, this dictionary-style syntax
can also be used to modify the object, in this case adding a new column:"]

(require '[tech.v3.datatype.functional :as dfn])

(assoc states :density (dfn// (states :pop) (states :area)))

["This shows a preview of the straightforward syntax of element-by-element
arithmetic between Column objects; we'll dig into this further in Operating on
Data in Dataset."]
