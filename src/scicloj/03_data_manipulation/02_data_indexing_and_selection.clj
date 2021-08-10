(ns scicloj.03-data-manipulation.02-data-indexing-and-selection
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]
            [clojure.java.io :as io]
            [tablecloth.api :as tablecloth]
            [tech.v3.dataset :as dataset]))

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
    ;; generate static site
  (notespace/render-static-html))

["In Chapter 2, we looked in detail at methods and tools to access, set, and
modify values in clojure vec and map. These included indexing (e.g., `(avec 2)`
`(amap :akey)` and `(:akey amap)` ) Here we'll look at similar means of
accessing and modifying values in Dataset objects. If you have used the clojure
patterns, the corresponding patterns in dataset will feel very familiar, though
there are a few quirks to be aware of."]


["# Data Selection in Dataset"]

["Recall that a Dataset acts in many ways like a map of columns. These analogies
can be helpful to keep in mind as we explore data selection within this
structure."]

["## Dataset as a map"]

["The first analogy we will consider is the Dataset as a map of related Column
objects. Let's return to our example of areas and populations of states:"]

(require '[tablecloth.api :as tablecloth])
(def names ["California" "Texas" "New York" "Florida" "Illinois"])
(def area [423967 695662 141297 170312 149995])
(def population [38332521 26448193 19651127 19552860 12882135])
(def states (tablecloth/dataset {:name names
                                 :area area
                                 :population population}))
^kind/dataset
states
;; => _unnamed [5 3]:
;;    |      :name |  :area | :population |
;;    |------------|-------:|------------:|
;;    | California | 423967 |    38332521 |
;;    |      Texas | 695662 |    26448193 |
;;    |   New York | 141297 |    19651127 |
;;    |    Florida | 170312 |    19552860 |
;;    |   Illinois | 149995 |    12882135 |

["The individual Column that make up the Dataset can be accessed via map-style
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

["To add new columns, we can use the add-columns API:"]

(require '[tech.v3.datatype.functional :as dfn])

(def states
  (tablecloth/add-columns states {:density #(dfn// (% :population) (% :area))}))
^kind/dataset
states
;; => _unnamed [5 4]:
;;    |      :name |  :area | :population | :density |
;;    |------------|-------:|------------:|---------:|
;;    | California | 423967 |    38332521 |       90 |
;;    |      Texas | 695662 |    26448193 |       38 |
;;    |   New York | 141297 |    19651127 |      139 |
;;    |    Florida | 170312 |    19552860 |      114 |
;;    |   Illinois | 149995 |    12882135 |       85 |


["This shows a preview of the straightforward syntax of element-by-element
arithmetic between Column objects; we'll dig into this further in Operating on
Data in Dataset."]

["## Dataset as two-dimensional vector"]

["As mentioned previously, we can also view the Dataset as an enhanced
two-dimensional vector. We can examine the raw underlying data array using the
values attribute:"]

;; TODO: how to convert all dataset to arrays?

["With this picture in mind, many familiar array-like observations can be done
on the Dataset itself. For example, we can transpose the full Dataset to swap
rows and columns:"]

;; TODO: How to transpose?

["When it comes to indexing of Dataset objects, however, it is clear that the
map-style indexing of columns precludes our ability to simply treat it as a
clojure vector. In particular, passing a single index to an array accesses a
row:"]

^kind/dataset
(tablecloth/select-rows states 0)
;; => _unnamed [1 4]:
;;    |      :name |  :area | :population | :density |
;;    |------------|-------:|------------:|---------:|
;;    | California | 423967 |    38332521 |       90 |

["and passing a single 'column name' to a Dataset accesses a column:"]

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

["Thus for array-style indexing, we need another convention. Here tablecloth
uses `select' method. The dataset column labels are maintained in the result:"]

^kind/dataset
(tablecloth/select states [:name :area :population] (range 3))
;; => _unnamed [3 3]:
;;    |      :name |  :area | :population |
;;    |------------|-------:|------------:|
;;    | California | 423967 |    38332521 |
;;    |      Texas | 695662 |    26448193 |
;;    |   New York | 141297 |    19651127 |

["Any of the familiar array-style data access patterns can be used within these
indexers. For example, in the `select' function we can combine masking and fancy
indexing as in the following:"]

^kind/dataset
(tablecloth/select states [:name :population :density] #(> (:density %) 100))
;; => _unnamed [2 3]:
;;    |    :name | :population | :density |
;;    |----------|------------:|---------:|
;;    | New York |    19651127 |      139 |
;;    |  Florida |    19552860 |      114 |

["Any of these indexing conventions may also be used to set or modify values:"]

;; TODO: update-columns cannot update with plain value

^kind/dataset
(tablecloth/update-columns states [:density] (fn [x] 90))
;; => _unnamed [5 4]:
;;    |      :name |  :area | :population | :density |
;;    |------------|-------:|------------:|---------:|
;;    | California | 423967 |    38332521 |       90 |
;;    |      Texas | 695662 |    26448193 |       90 |
;;    |   New York | 141297 |    19651127 |       90 |
;;    |    Florida | 170312 |    19552860 |       90 |
;;    |   Illinois | 149995 |    12882135 |       90 |

["To build up your fluency in dataset data manipulation, I suggest spending some
time with a simple DataFrame and exploring the types of indexing, slicing,
masking, and fancy indexing that are allowed by these various indexing
approaches."]

["## Additional indexing conventions"]

["There are a couple extra indexing conventions that might seem at odds with the
preceding discussion, but nevertheless can be very useful in practice. First,
while indexing refers to columns, slicing refers to rows:"]

;; TODO

["select rows with seq of row ids and seq of true/false:"]

;; TODO

["Such slices can also refer to rows by number rather than by index:"]

^kind/dataset
(tablecloth/select-rows states [0 1 2])
;; => _unnamed [3 4]:
;;    |      :name |  :area | :population | :density |
;;    |------------|-------:|------------:|---------:|
;;    | California | 423967 |    38332521 |       90 |
;;    |      Texas | 695662 |    26448193 |       38 |
;;    |   New York | 141297 |    19651127 |      139 |

^kind/dataset
(tablecloth/select-rows states [true false true])
;; => _unnamed [2 4]:
;;    |      :name |  :area | :population | :density |
;;    |------------|-------:|------------:|---------:|
;;    | California | 423967 |    38332521 |       90 |
;;    |   New York | 141297 |    19651127 |      139 |

["tech.ml.dataset has additional 'select-by-index' interface, to specify column
and row index in vector:"]

^kind/dataset
(dataset/select-by-index states [0 1] [0 2])
;; => _unnamed [2 2]:
;;    |      :name |  :area |
;;    |------------|-------:|
;;    | California | 423967 |
;;    |   New York | 141297 |

["or 'select-columns-by-index':"]

^kind/dataset
(dataset/select-columns-by-index states [0 3])
;; => _unnamed [5 2]:
;;    |      :name | :density |
;;    |------------|---------:|
;;    | California |       90 |
;;    |      Texas |       38 |
;;    |   New York |      139 |
;;    |    Florida |      114 |
;;    |   Illinois |       85 |

["you can even use negative index:"]

(dataset/select-rows-by-index states [-1 0 -2])
;; => _unnamed [3 4]:
;;    |      :name |  :area | :population | :density |
;;    |------------|-------:|------------:|---------:|
;;    |   Illinois | 149995 |    12882135 |       85 |
;;    | California | 423967 |    38332521 |       90 |
;;    |    Florida | 170312 |    19552860 |      114 |

["Similarly, direct masking operations are also interpreted row-wise rather than
column-wise:"]

^kind/dataset
(tablecloth/select-rows states #(> (:density %) 100))
;; => _unnamed [2 4]:
;;    |    :name |  :area | :population | :density |
;;    |----------|-------:|------------:|---------:|
;;    | New York | 141297 |    19651127 |      139 |
;;    |  Florida | 170312 |    19552860 |      114 |
