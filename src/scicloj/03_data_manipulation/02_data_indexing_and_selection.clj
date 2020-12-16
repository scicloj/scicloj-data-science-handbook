(ns scicloj.03-data-manipulation.02-data-indexing-and-selection
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

^kind/dataset
(states :area)
;; => #tech.v3.dataset.column<int64>[5]
;;    :area
;;    [423967, 695662, 141297, 170312, 149995, ]

["Equivalently, we can use keyword-style access with column names that are
keywords:"]

^kind/dataset
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

(def states (assoc states :density (dfn// (states :pop) (states :area))))

["This shows a preview of the straightforward syntax of element-by-element
arithmetic between Column objects; we'll dig into this further in Operating on
Data in Dataset."]

["## Dataset as two-dimensional array"]

["As mentioned previously, we can also view the Dataset as an enhanced
two-dimensional array. We can examine the raw underlying data array using the
values attribute:"]

;; TODO how to convert all dataset to arrays?

["With this picture in mind, many familiar array-like observations can be done
on the DataFrame itself. For example, we can transpose the full DataFrame to
swap rows and columns:"]

;; TODO: How to transpose?

["When it comes to indexing of Dataset objects, however, it is clear that the
map-style indexing of columns precludes our ability to simply treat it as a
NumPy array. In particular, passing a single index to an array accesses a row:"]

^kind/dataset
(tablecloth/select-rows states 0)

["and passing a single \"index\" to a Dataset accesses a column:"]

^kind/dataset
(states :area)

["Thus for array-style indexing, we need another convention. Here Pandas again
uses the loc, iloc, and ix indexers mentioned earlier. Using the iloc indexer,
we can index the underlying array as if it is a simple NumPy array (using the
implicit Python-style index), but the DataFrame index and column labels are
maintained in the result:"]

;; TODO: loc, iloc and ix is not available in dataset

^kind/dataset
(tablecloth/select states [:name :area :pop] (range 3))

["Any of the familiar NumPy-style data access patterns can be used within these
indexers. For example, in the loc indexer we can combine masking and fancy
indexing as in the following:"]

^kind/dataset
(tablecloth/select states [:name :pop :density] #(> (:density %) 100))

["Any of these indexing conventions may also be used to set or modify values;
this is done in the standard way that you might be accustomed to from working
with NumPy:"]

;; TODO: update-columns cannot update with value
^kind/dataset
(tablecloth/update-columns states [:density] (fn [x] 90))

["To build up your fluency in Pandas data manipulation, I suggest spending some
time with a simple DataFrame and exploring the types of indexing, slicing,
masking, and fancy indexing that are allowed by these various indexing
approaches."]

["## Additional indexing conventions"]

["There are a couple extra indexing conventions that might seem at odds with the
preceding discussion, but nevertheless can be very useful in practice. First,
while indexing refers to columns, slicing refers to rows:"]

["select rows with seq of row ids and seq of true/false:"]

["Such slices can also refer to rows by number rather than by index:"]

^kind/dataset
(tablecloth/select-rows states [0 1 2])

^kind/dataset
(tablecloth/select-rows states [true false true])

["Similarly, direct masking operations are also interpreted row-wise rather than
column-wise:"]

^kind/dataset
(tablecloth/select-rows states #(> (:density %) 100))

["These two conventions are syntactically similar to those on a NumPy array, and
while these may not precisely fit the mold of the Pandas conventions, they are
nevertheless quite useful in practice."]
