(ns scicloj.03-data-manipulation.03-operations-in-dataset
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

["### Index alignment in Dataset"]

["A similar type of alignment takes place for both columns and indices when
performing operations on DataFrames:"]

(def A
  (tablecloth/dataset
   (zipmap [:A :B]
           (repeatedly 2 (fn [] (repeatedly 2 #(fm.rand/irand 20)))))))
^kind/dataset
A
;; => _unnamed [2 2]:
;;    | :A | :B |
;;    |----|----|
;;    |  1 |  7 |
;;    |  7 | 18 |

(def B
  (tablecloth/dataset
   (zipmap [:B :A :C]
           (repeatedly 3 (fn [] (repeatedly 3 #(fm.rand/irand 20)))))))
^kind/dataset
B
;; => _unnamed [3 3]:
;;    | :B | :A | :C |
;;    |----|----|----|
;;    |  8 |  5 | 11 |
;;    |  2 | 15 | 13 |
;;    |  9 |  3 |  3 |

;; TODO: How to A + B?
;; (tablecloth/+ A B)?

["Notice that indices are aligned correctly irrespective of their order in the
two objects, and indices in the result are sorted. As was the case with Series,
we can use the associated object's arithmetic method and pass any desired
fill_value to be used in place of missing entries. Here we'll fill with the mean
of all values in A (computed by first stacking the rows of A):

```python
fill = A.stack().mean()
A.add(B, fill_value=fill)
```
"]

["The following table lists Python operators and their equivalent Pandas object
methods:
```
+ 	add()
- 	sub(), subtract()
* 	mul(), multiply()
/ 	truediv(), div(), divide()
// 	floordiv()
% 	mod()
** 	pow()
```"]

["## Ufuncs: Operations Between DataFrame and Series"]

["When performing operations between a DataFrame and a Series, the index and
column alignment is similarly maintained. Operations between a DataFrame and a
Series are similar to operations between a two-dimensional and one-dimensional
NumPy array. Consider one common operation, where we find the difference of a
two-dimensional array and one of its rows:"]

;; TODO ...
