(ns scicloj.03-data-manipulation.03-operations-in-dataset
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

["# Operating on Data in Dataset"]

["One of the essential pieces of Dataset is the ability to perform quick
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
  '[tech.v3.dataset.column :as dcol]
  '[tablecloth.api :as tablecloth])

["## Index Preservation"]

(def DS
  (tablecloth/dataset
   (zipmap [:A :B :C :D]
           (repeatedly 4 (fn [] (repeatedly 3 #(rand 10)))))))

^kind/dataset
DS
;; => _unnamed [3 4]:
;;    |         :A |         :B |         :C |         :D |
;;    |-----------:|-----------:|-----------:|-----------:|
;;    | 9.60562809 | 8.55226726 | 2.23421807 | 4.00705231 |
;;    | 4.04167810 | 8.44119331 | 5.65535994 | 2.44606411 |
;;    | 5.81259846 | 7.59271646 | 0.31749207 | 6.31114879 |


["If we apply a datatype functional func on either of these objects, the result
will be another dataset object with the indices preserved:"]

^kind/dataset
(ds/update-elemwise DS dfn/exp)
;; => _unnamed [3 4]:
;;    |             :A |            :B |           :C |           :D |
;;    |---------------:|--------------:|-------------:|-------------:|
;;    | 14848.11334084 | 5178.48210835 |   9.33917639 |  54.98455391 |
;;    |    56.92178329 | 4634.08158596 | 285.81934239 |  11.54282595 |
;;    |   334.48714785 | 1983.69482239 |   1.37367836 | 550.67719712 |

["Or, for a slightly more complex calculation:"]

^kind/dataset
(ds/update-elemwise DS #(dfn// (dfn/* % Math/PI) 4.0))
;; => _unnamed [3 4]:
;;    |         :A |         :B |         :C |         :D |
;;    |-----------:|-----------:|-----------:|-----------:|
;;    | 7.54424266 | 6.71693500 | 1.75475077 | 3.14713152 |
;;    | 3.17432656 | 6.62969772 | 4.44170931 | 1.92113426 |
;;    | 4.56520415 | 5.96330556 | 0.24935769 | 4.95676467 |


["Any of the datatype functional functions can be used in a similar manner."]

["## Index Alignment"]

["For binary operations on two Dataset objects, it will align indices in the
process of performing the operation. This is very convenient when working with
incomplete data, as we'll see in some of the examples that follow."]

["### Index alignment in Dataset"]

["A similar type of alignment takes place for both columns and indices when
performing operations on Datasets:"]

(def A
  (tablecloth/dataset
   (zipmap [:A :B]
           (repeatedly 2 (fn [] (repeatedly 2 #(rand-int 20)))))))

^kind/dataset
A;; => _unnamed [2 2]:
;;    | :A | :B |
;;    |---:|---:|
;;    | 16 |  7 |
;;    |  3 |  1 |

(def B
  (tablecloth/dataset
   (zipmap [:B :A :C]
           (repeatedly 3 (fn [] (repeatedly 3 #(rand-int 20)))))))

^kind/dataset
B;; => _unnamed [3 3]:
;;    | :B | :A | :C |
;;    |---:|---:|---:|
;;    | 18 |  0 |  9 |
;;    |  8 | 12 |  8 |
;;    | 16 |  6 |  7 |


["Now, lets create some function to add two dataset in different
shape. The prerequisite is that these two dataset contains numerical
values."]

(defn add-columns-with-same-name [ds colname]
  (->> (tablecloth/select-columns ds colname)
       (ds/columns)
       (reduce dfn/+)
       (ds/new-column colname)
       (vector)
       (ds/new-dataset)))

(defn aggregate-columns [ds]
  (let [col-names (distinct (tablecloth/column-names ds))]
    (->> col-names
         (map #(add-columns-with-same-name ds %))
         (reduce tablecloth/append))))

(defn dataset-add [ds1 ds2]
  (aggregate-columns (tablecloth/append ds1 ds2)))

["TODO: Oops, the answer is wrong. tablecloth/select-columns will only
duplicate the first specified column twice!"]

^kind/dataset
(dataset-add A B)
;; => _unnamed [3 3]:
;;    | :A | :B | :C |
;;    |---:|---:|---:|
;;    |  0 | 36 |  9 |
;;    | 24 | 16 |  8 |
;;    | 12 | 32 |  7 |


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
