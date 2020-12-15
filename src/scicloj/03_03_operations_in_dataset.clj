(ns scicloj.03-03-operations-in-dataset
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
  (notespace/render-static-html "docs/scicloj/ch03/03_03_operations_in_dataset.html"))

["# Operating on Data in Dataset"]

["One of the essential pieces of NumPy is the ability to perform quick
element-wise operations, both with basic arithmetic (addition, subtraction,
multiplication, etc.) and with more sophisticated operations (trigonometric
functions, exponential and logarithmic functions, etc.). Pandas inherits much of
this functionality from NumPy, and the ufuncs that we introduced in Computation
on NumPy Arrays: Universal Functions are key to this.

Dataset includes a couple useful twists, however: for unary operations like
negation and trigonometric functions, these ufuncs will preserve index and
column labels in the output, and for binary operations such as addition and
multiplication, Pandas will automatically align indices when passing the objects
to the ufunc. This means that keeping the context of data and combining data
from different sources–both potentially error-prone tasks with raw NumPy
arrays–become essentially foolproof ones with Pandas. We will additionally see
that there are well-defined operations between one-dimensional Series structures
and two-dimensional DataFrame structures."]

(require
  '[tech.v3.dataset :as ds]
  '[tech.v3.datatype :as dtype]
  '[tech.v3.datatype.functional :as dfn]
  '[tablecloth.api :as tablecloth]
  '[fastmath.random :as fm.rand])

(def DS
  (tablecloth/dataset
   (zipmap [:A :B :C :D]
           (repeatedly 4 (fn [] (repeatedly 3 #(fm.rand/frand 10)))))))

^kind/dataset
DS

["If we apply a NumPy ufunc on either of these objects, the result will be
another Pandas object with the indices preserved:"]

^kind/dataset
(ds/update-elemwise DS dfn/exp)

["Or, for a slightly more complex calculation:"]

^kind/dataset
(ds/update-elemwise DS #(dfn// (dfn/* % Math/PI)))

["Any of the ufuncs discussed in Computation on NumPy Arrays: Universal
Functions can be used in a similar manner."]

["## UFuncs: Index Alignment"]

["For binary operations on two Series or DataFrame objects, Pandas will align
indices in the process of performing the operation. This is very convenient when
working with incomplete data, as we'll see in some of the examples that
follow."]

["### Index alignment in DataFrame"]

["A similar type of alignment takes place for both columns and indices when
performing operations on DataFrames:"]

(def A
  (tablecloth/dataset
   (zipmap [:A :B]
           (repeatedly 2 (fn [] (repeatedly 2 #(fm.rand/frand 20)))))))
^kind/dataset
A

(def B
  (tablecloth/dataset
   (zipmap [:B :A :C]
           (repeatedly 3 (fn [] (repeatedly 3 #(fm.rand/frand 20)))))))
^kind/dataset
B

;; TODO: How to A + B?
