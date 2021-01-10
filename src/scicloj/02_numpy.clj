(ns scicloj.02-numpy
  (:require [notespace.api]
            [notespace.kinds :as k]
            [notespace.state :as state]))

^k/hidden
(comment
  (notespace.api/init)
  (notespace.api/init-with-browser)
  (notespace.api/eval-this-notespace)
  (notespace.api/eval-note-at-line 14)

  (notespace.api/listen)
  (notespace.api/unlisten)
  nil)



["
[Python Data Science - ch.2. NumPy translated to Clojure](https://jakevdp.github.io/PythonDataScienceHandbook/02.06-boolean-arrays-and-masks.html)
=======================================================

"]

["Understanding Data Types in Python
 ------------------------------------------------"]

["### Fixed-Type Arrays in Python

In Clojure we use [dtype-next](https://github.com/cnuernber/dtype-next) (also known as
`tech.v3.datatype`) and the convenient wrapper with a consistent API, [tablecloth](https://scicloj.github.io/tablecloth/),
for working efficiently with dataset data."]
(require
 '[tech.v3.dataset :as ds]
 '[tech.v3.datatype :as dtype]
 '[tech.v3.tensor :as dtt]
 '[tablecloth.api :as api])

^k/hidden
(declare index)
^k/hidden
(defn ravel
  "See `numpy.ravel`"
  [t]
  (let [shape (dtype/shape (dtt/->tensor t))
        ix    (index shape)]
    (dtype/make-reader :object (apply * shape) (apply dtt/mget t (nth ix idx)))))

["### Creating Arrays from ~Python~ Lists

In Python:

```python
# integer array:
np.array([1, 4, 2, 5, 3])
```

In Clojure we mostly don't work with low-level \"arrays\" but with \"datasets\".
In 2D those consist of rows and (named) columns:
"]

(api/dataset {:column1 [1 4 2 5 3]})

["Datasets are provided by `tech.v3.dataset` and made convenient via Tablecloth.

The underlying data structure - from `tech.v3.datatype` - is a \"container\", very similar to numpy arrays. From the [dtype Cheatshet](https://cnuernber.github.io/dtype-next/cheatsheet.html):

> There are two different types of containers in tech.v3.datatype; jvm-heap containers and native-heap containers. Object datatypes are only supported in jvm-heap containers and native-heap containers have mmap support and offer zero-copy pathways to toolkits like Python’s numpy and Clojure’s neanderthal."]

@(def f32 (dtype/make-container :jvm-heap :float32 [1 4 2 5 3]))

["Containers are space- and time-efficient data structures with fast copy, including from/to native memory (for sharing with external processes).

A container is backed by a raw, typed _buffer_. A read-only view of a buffer is called _reader_.

For multi-dimensional data, dtype-next offers *tensors*:

> Generic N-dimensional support built on top of buffers and dimension objects. Conceptually you combine a raw data buffer with an indexing mechanism capable if transforming multiple dimension address into a linear address into the raw data buffer.

(We will see more of tensors later.)"]

["A dataset column is constrained to a single value type. If types do not match, they will be upcasted if possible (here, integers are up-cast to floating point):"]
(api/dataset {:v [3.14, 4, 2, 3]} {:dataset-name "Mixed int and float"})

["### Creating Arrays from Scratch

Create an empty array of length 5:"]
(dtype/make-container :jvm-heap :float16 5)

["Numpy has a number of ways of initializing new arrays with values:"]

["Create a length-10 integer array filled with zeros
```python
np.zeros(10, dtype=int)
```"]
(dtype/emap (constantly 0) nil
            (dtype/make-container :jvm-heap :int8 10))

["Create a 3x5 floating-point array filled with ones
```python
np.ones((3, 5), dtype=float)
```"]
(dtype/emap (constantly 1) nil
            (dtt/new-tensor [3 5]))

["Create a 3x5 floating-point array filled with 3.14
```python
np.full((3, 5), 3.14)
```"]
(dtype/emap (constantly 3.14) nil
            (dtt/new-tensor [3 5]))

["Create an array filled with a linear sequence
Starting at 0, ending at 20, stepping by 2
```python
np.arange(0, 20, 2)
```"]
(dtype/make-container :jvm-heap :int8 (range 0 20 2))

["Create an array of five values evenly spaced between 0 and 1 ❓
```python
np.linspace(0, 1, 5)
"]

(defn ^{:doc "Temporary workaround, maybe there is a better way to do this"}
  linspace
  ([start stop] (linspace start stop 50))
  ([start stop n]
   (let [delta (- stop start)
         end   (dec n)]
     (dtt/->tensor (map (fn [i] (/ (* i delta) end))
                        (range n))))))

(linspace 0 1 5)

["Create a 3x3 array of uniformly distributed random values between 0 and 1
```python
np.random.random((3, 3))
```"]
(require '[fastmath.random :as fm.rand])
(dtype/emap (fn [_] (fm.rand/frand)) nil
            (dtt/new-tensor [3 3]))

["Create a 3x3 array of normally distributed random values with mean 0 and standard deviation 1
```python
np.random.normal(0, 1, (3, 3))
```"]
(dtype/emap (fn [_] (fm.rand/grand 0 1)) nil
            (dtt/new-tensor [3 3]))

["Create a 3x3 array of random integers in the interval [0, 10]
```python
np.random.randint(0, 10, (3, 3))
```"]
(dtype/emap (fn [_] (fm.rand/irand 0 11)) nil
            (dtt/new-tensor [3 3]))

["Create a 3x3 identity matrix
```python
np.eye(3)
```"]

["We currently do not have a good approximation of this technique.  However,
it is easy enough to compute an identity matrix using an outer product operation."]

(defn index
  "Like range, but for tensors.  Enumerates the indicies."
  [shape]
  (let [space  (apply * shape)
        rshape (reverse shape)
        muls   (cons 1 (pop (vec (reductions * rshape))))]
    (map
     (fn [idx]
       (reverse
        (map (fn [p n]
               (-> idx
                   (quot n)
                   (mod p)))
             rshape
             muls)))
     (range space))))

(index [3 3])

(defn outer-product
  ;; Author: Chris Nuernberger
  ;; https://github.com/scicloj/scicloj-data-science-handbook/pull/2#discussion_r548033202
  [f a b]
  (let [a-shape (dtype/shape a)
        b-shape (dtype/shape b)
        a-rdr   (dtype/->reader a)
        b-rdr   (dtype/->reader b)
        n-b     (.lsize b-rdr)
        n-elems (* (.lsize a-rdr) n-b)]
    ;;Doing the cartesian join is easier in linear space
    (-> (dtype/emap
         (fn [^long idx]
           (let [a-idx (quot idx n-b)
                 b-idx (rem idx n-b)]
             (f (a-rdr a-idx) (b-rdr b-idx))))
         :object
         (range n-elems))
        (dtt/reshape (concat a-shape b-shape)))))

(defn eye [n]
  (letfn [(= [a b] (if (clojure.core/= a b) 1 0))]
    (outer-product = (dtt/->tensor (range n))
                   (dtt/->tensor (range n)))))

(eye 3)

(eye 5)

["Create an uninitialized array of three integers. The values will be whatever happens to already exist at that memory location
```python
np.empty(3)
```"]
(dtype/make-container :jvm-heap :float32 3)

["### NumPy Standard Data Types

```
Data type 	Description
bool_ 	Boolean (True or False) stored as a byte
int_ 	Default integer type (same as C long; normally either int64 or int32)
intc 	Identical to C int (normally int32 or int64)
intp 	Integer used for indexing (same as C ssize_t; normally either int32 or int64)
int8 	Byte (-128 to 127)
int16 	Integer (-32768 to 32767)
int32 	Integer (-2147483648 to 2147483647)
int64 	Integer (-9223372036854775808 to 9223372036854775807)
uint8 	Unsigned integer (0 to 255)
uint16 	Unsigned integer (0 to 65535)
uint32 	Unsigned integer (0 to 4294967295)
uint64 	Unsigned integer (0 to 18446744073709551615)
float_ 	Shorthand for float64.
float16 	Half precision float: sign bit, 5 bits exponent, 10 bits mantissa
float32 	Single precision float: sign bit, 8 bits exponent, 23 bits mantissa
float64 	Double precision float: sign bit, 11 bits exponent, 52 bits mantissa
complex_ 	Shorthand for complex128.
complex64 	Complex number, represented by two 32-bit floats
complex128 	Complex number, represented by two 64-bit floats
```

dtype-next data types:

```
:boolean
:char
:int8
:int16
:int32
:int64
:uint8
:uint16
:uint32
:uint64
:float32
:float64
```"]

["The Basics of NumPy Arrays
 ------------------------------------------------"]
["### NumPy Array Attributes

`ndim, size, shape` (list of dimension lengths), `dtype` e.g. int64,
`itemsize` in bytes and the total `nbytes` count."]

(count f32)
(dtype/get-datatype f32)
;; nothing for itemsize, nbytes? => need to check java docs and count oneself

["### Array Indexing: Accessing Single Elements

Ex.: `x[4]`, `x[-1]`, `x[1,2]`"]

["Accessing nth element of a container, e.g. for n=2:"]

(f32 1)
(nth f32 1)

["### Array Slicing: Accessing Subarrays

`x[start:stop:step]`, e.g. `x[::2]  # every other element`, `x[5::-2]  # reversed every other from index 5`"]

;; see https://cnuernber.github.io/dtype-next/tech.v3.datatype.html#var-sub-buffer - not sure if possible to somehow define the "step" - perhaps using argops and indexed-buffer

(def x (dtt/->tensor (range 20)))

;; defined below
(declare select)

(defn array-slice
  ([t stop]
   (dtt/select t (range stop)))
  ([t start stop]
   (dtt/select t (range start stop)))
  ([t start stop step]
   (dtt/select t (range start stop step))))


["These work as expected"]
(array-slice x 0 20 2)
(array-slice x 19 0 -2)

["### Multi-dimensional subarrays

Same as for 1D arrays.

`x2[:2, :3]  # two rows, three columns`

Clj: You can get/set subrects at a given time using mget/mset! pathways from `tech.v3.tensor`."]


["#### Accessing array rows and columns

One commonly needed routine is accessing of single rows or columns of an array.

`print(x2[:, 0])  # first column of x2`
`print(x2[0, :])  # first row of x2`"]

(defn select [t & coords]
  (let [shape-t (dtype/shape t)
        coords  (map-indexed (fn [i c]
                               (if (or (nil? c) (empty? c))
                                 (range (nth shape-t i))
                                 c))
                             coords)]
    (apply dtt/select t coords)))

(def t (-> (dtt/->tensor (range (* 2 3)))
           (dtt/reshape [2 3])))

(select t nil [0])
(select t [0] nil)


["#### Subarrays as no-copy views

views rather than copies of the array data

NOTE: You can use `tech.v3.datatype.argops` to create \"indexes\" for a buffer and then
combine these with the buffer using `tech.v3.datatype/indexed-buffer` to create a custom
view of the original buffer"]

;; todo -- does something need to be done for this entry?

["#### Creating copies of arrays

Despite the nice features of array views, it is sometimes useful to instead explicitly copy the data within an array or a subarray."]

;; see "Copy" in https://cnuernber.github.io/dtype-next/cheatsheet.html
;; see https://cnuernber.github.io/dtype-next/tech.v3.datatype.html#var-copy.21

["### Reshaping of Arrays

For example, if you want to put the numbers 1 through 9 in a 3×3 grid"]

(-> (dtt/->tensor (range 1 10))
    (dtt/reshape [3 3]))


["### Array Concatenation and Splitting

All of the preceding routines worked on single arrays. It's also possible to combine multiple arrays into one, and to conversely split a single array into multiple arrays. "]

;; see https://scicloj.github.io/tablecloth/#JoinConcat_Datasets
;; see https://cnuernber.github.io/dtype-next/tech.v3.datatype.html#var-concat-buffers

["#### Concatenation of arrays

NumPy: `np.concatenate, np.vstack, and np.hstack`"]


(defn rank [t] (dec (count (dtype/shape t))))

(defn concatenate
  ([a b] (concatenate (rank a) a b))
  ([r a b]
   (let [left-shape        (dtype/shape a)
         right-shape       (dtype/shape b)
         left-join-rank    (nth (dtype/shape a) r)
         right-join-rank   (nth (dtype/shape b) r)
         left-cross-shape  (map-indexed (fn [i c] (if (= i r) nil (range c)))
                                        (dtype/shape a))
         right-cross-shape (map-indexed (fn [i c] (if (= i r) nil (range c)))
                                        (dtype/shape b))
         left-cross        (filter some? left-cross-shape)
         right-cross       (filter some? right-cross-shape)
         crosses           (map dtt/->tensor (interleave left-cross right-cross))
         final-shape       (map-indexed
                            (fn [i d]
                              (if (= i r) (+ left-join-rank right-join-rank) d))
                            left-shape)
         idx'              (dtype/emap
                            (fn [idx]
                              (let [special-rank  (nth idx r)
                                    target-tensor (if (< special-rank left-join-rank) a b)
                                    special-idx   (if (< special-rank left-join-rank)
                                                    special-rank
                                                    (- special-rank left-join-rank))
                                    target-coords
                                    (map-indexed (fn [i d] (if (= i r) special-idx d)) idx)]
                                (apply dtt/mget target-tensor target-coords)))
                            :object
                            (dtype/->reader (vec (index final-shape))))]
     (dtt/reshape idx' final-shape))))


(def t23 (dtt/->tensor [[1 2 3] [4 5 6]]))
(def t22 (dtt/->tensor [["a" "b"] ["c" "d"]]))

(dtt/->tensor [(concat (dtype/->reader (select t23 [0] nil)) (dtype/->reader (select t22 [0] nil)))
               (concat (dtype/->reader (select t23 [1] nil)) (dtype/->reader (select t22 [1] nil)))])

(concatenate t23 t22)

(-> (concatenate 0 t23 t23)
    (concatenate (concatenate 0 t22 t22)))

(def t235 (-> (dtt/->tensor (range (* 2 3 5)))
              (dtt/reshape [2 3 5])))

(concatenate 0 t235 t235)

(concatenate 1 t235 t235)

(concatenate 2 t235 t235)


["#### Splitting of arrays

The opposite of concatenation is splitting, which is implemented by the functions `np.split`, `np.hsplit`, `np.vsplit`, and `np.dsplit` [depth?]. For each of these, we can pass a list of indices giving the split points:"]

(defn normalize [t]
  (let [min (tech.v3.datatype.functional/min t (apply min (ravel t)))
        max (tech.v3.datatype.functional/max t (apply max (ravel t)))
        n   (first (dtype/shape t))]
    (tech.v3.datatype.functional//
     (tech.v3.datatype.functional/- t min)
     (tech.v3.datatype.functional/- max min))))

(normalize t)

(defn split
  ([t pieces] (split 0 t pieces))
  ([axis t pieces]
   (into []
         (comp 
          (map (fn [x] (range (first x) (second x))))
          (map #(conj (mapv (constantly nil) (range axis)) %))
          (map #(apply select t %)))
         (->> (dtype/emap #(-> %
                               Math/floor
                               int)
                          :object
                          (if-let [v (and  (vector? pieces)
                                           (-> (normalize (dtt/reshape (dtt/->tensor pieces)
                                                                       [1 (count pieces)]
                                                                       ))
                                               (dtt/reshape [(count pieces)])))]
                            ;; individual element selection
                            (tech.v3.datatype.functional/* v
                                                           (nth (dtype/shape t) axis))
                            ;; number of partitions selection
                            (tech.v3.datatype.functional/* (linspace 0 1 (inc pieces))
                                                           (nth (dtype/shape t) axis))))
              (partition 2 1)))))

(def t (-> (dtt/->tensor (range (* 2 3 5)))
           (dtt/reshape [2 3 5])))

(let [t (->> (concatenate 0 t t)
             (concatenate 0 t))]
  (split 2 t 5))

(let [t (->> (concatenate 0 t t)
             (concatenate 0 t))]
  (split 2 t [2 3 4 2]))






["Computation on NumPy Arrays: Universal Functions
 ------------------------------------------------

 Computation on NumPy arrays can be very fast, or it can be very slow. The key to making it fast is to use vectorized operations, generally implemented through NumPy's universal functions (ufuncs). This section motivates the need for NumPy's ufuncs, which can be used to make repeated calculations on array elements much more efficient. It then introduces many of the most common and useful arithmetic ufuncs available in the NumPy package."]

["#### Introducing UFuncs

Ex.:
* `1.0 / matrice`
* `np.arange(5) / np.arange(1, 6)` - two arrays
* `x = np.arange(9).reshape((3, 3)); 2 ** x` - on multi-dimensional array

dtype-next offers [`tech.v3.datatype.functional`](https://cnuernber.github.io/dtype-next/tech.v3.datatype.functional.html)"]

;; see https://cnuernber.github.io/dtype-next/tech.v3.datatype.functional.html
;; see https://cnuernber.github.io/dtype-next/tech.v3.datatype.html#var-emap

(tech.v3.datatype.functional// 1.0 t)

(tech.v3.datatype.functional// (dtt/->tensor (map float (range 5)))
                               (dtt/->tensor (map float (range 1 6))))


(-> (range 9)
    (dtt/->tensor)
    (dtt/reshape [3 3])
    (->> (dtype/emap #(Math/pow % 2) nil)))

["### Exploring NumPy's UFuncs"]
["#### Array arithmetic

* addition, subtraction, multiplication, and division (+ `floor_divide`).
* unary ufunc for negation, and a ** operator for exponentiation, and a % operator for modulus

these can be strung together however you wish, and the standard order of operations is respecte"]


["Check this table for all kinds of goodies available in `tech.v3.datatype.functional`"]
(->> (ns-publics 'tech.v3.datatype.functional)
     (map first)
     sort
     (take 100)
     (dtt/->tensor)
     (#(dtt/reshape % [25 4])))



["### Absolute value"]
["### Trigonometric functions

```
theta = np.linspace(0, np.pi, 3)
print(\"theta      = \", theta)
print(\"sin(theta) = \", np.sin(theta))
print(\"cos(theta) = \", np.cos(theta))
print(\"tan(theta) = \", np.tan(theta))
np.arcsin(x)
np.arccos(x)
np.arctan(x)
```"]




["#### Exponents and logarithms

* e^x, 2^x, n^x
* ln(x), log2(x), log10(x)
* np.expm1(x) = exp(x) - 1, np.log1p(x) = log(1 + x)"]



["#### Specialized ufuncs

NumPy has many more ufuncs available, including hyperbolic trig functions, bitwise arithmetic, comparison operators, conversions from radians to degrees, rounding and remainders, and much more. A look through the NumPy documentation reveals a lot of interesting functionality.

Another excellent source for more specialized and obscure ufuncs is the submodule scipy.special. If you want to compute some obscure mathematical function on your data, chances are it is implemented in scipy.special. There are far too many functions to list them all..."]

["`dtype/emap` will serve the vast majority of those needs"]


["### Advanced Ufunc Features"]

["#### Specifying output

For large calculations, it is sometimes useful to be able to specify the array where the result of the calculation will be stored. Rather than creating a temporary array, this can be used to write computation results directly to the memory location where you'd like them to be. For all ufuncs, this can be done using the out argument of the function"]

["#### Aggregates

For binary ufuncs, there are some interesting aggregates that can be computed directly from the object. For example, if we'd like to reduce an array with a particular operation, we can use the reduce method of any ufunc. A reduce repeatedly applies a given operation to the elements of an array until only a single result remains.\n\nFor example, calling reduce on the add ufunc returns the sum of all elements in the array:

```
x = np.arange(1, 6)
np.add.reduce(x)      # 15
np.multiply.reduce(x) # 120
np.add.accumulate(x)  # [1, 3, ..] - store all the intermediate results of reduce

```"]

;; todo -- multiaxis reduce

["#### Outer products

Finally, any ufunc can compute the output of all pairs of two different inputs using the `outer` method. This allows you, in one line, to do things like create a multiplication table:

```
x = np.arange(1, 6)
np.multiply.outer(x, x)
```"]

(let [x (dtt/->tensor (range 1 6))]
  (outer-product * x x ))

["### Ufuncs: Learning More

More information on universal functions (including the full list of available functions) can be found on the NumPy and SciPy documentation websites."]

["Aggregations: Min, Max, and Everything In Between
 ------------------------------------------------

 Often when faced with a large amount of data, a first step is to compute summary statistics for the data in question. Perhaps the most common summary statistics are the mean and standard deviation, which allow you to summarize the \"typical\" values in a dataset, but other aggregates are useful as well (the sum, product, median, minimum and maximum, quantiles, etc.).

 NumPy has fast built-in aggregation functions for working on arrays; we'll discuss and demonstrate some of them here."]

["### Clojure: descriptive stats

`ds/descriptive-stats` displays these stats: `n-valid`, `n-missing`, `min`, `mean`, `mode`, `max`, `standard-deviation`, `skew`"]

; (ds/descriptive-stats csv-data)

["### Summing the Values in an Array

`np.sum(L)`"]


#_(defn ^:private reduce-axis
    ;; [WIP] dimensionality issue that doesn't conform to APL standard -- ultimately 
    ;; this is the more performant version
    "Author: Chris Nuernberger
  
  https://github.com/scicloj/scicloj-data-science-handbook/pull/2#discussion_r547591691"
  ([reduce-fn tensor]
   (reduce-axis reduce-fn tensor (-> tensor rank first dec)))
  ([reduce-fn tensor axis]
   (let [axis         (or axis (-> tensor rank first dec))
         rank         (count (dtype/shape tensor))
         dec-rank     (dec rank)
         axis         (if (>= axis 0)
                        axis
                        (+ rank axis))
         shape-idxes  (remove #(= axis %) (range rank))
         ;;transpose the tensor so the reduction axis is the last one
         tensor       (if-not (= dec-rank axis)
                        (dtt/transpose tensor (concat shape-idxes [axis]))
                        tensor)
         ;;slice to produce n sequence of data
         slices       (dtt/slice tensor dec-rank)
         result-shape (mapv (dtype/shape tensor) shape-idxes)]
     (-> (dtype/emap reduce-fn
                     :object
                     slices)
         ;;reshape to the result shape
         (dtt/reshape result-shape)))))

(defn ndreduce
  ([f t] (ndreduce (dec (count (dtype/shape t))) f t))
  ([axis f t]
   (let [
         shape            (dtype/shape t)
         select-shape     (map-indexed (fn [i x]
                                         (if  (= i axis)
                                           nil
                                           x))
                                       shape)
         index-shape      (filter some? select-shape)
         reduce-dimension (nth shape axis)]
     (transduce
      (comp
       (map (fn [coords] (apply dtt/mget t coords)))
       (partition-all reduce-dimension)
       (map (partial apply f))
            
       )
      (fn ([res] res
           (-> res
               persistent!
               (dtt/->tensor)
               (dtt/reshape index-shape)))
        ([res next] (conj! res next)))
      (transient [])
      (for [idx (index index-shape)
            a   (range reduce-dimension)]
        (map (fn [i]
               (cond (= i axis) a
                     (< i axis) (nth idx i)
                     :else      (nth idx (dec i))))
             (range (count select-shape))))))))

#_(defn nd-reduce
    ;; for when reduce-axis is brought online
    ([f t]
     (ndreduce  f t (-> t rank first dec)))
  ([axis f t]
   (reduce-axis (partial reduce f) t axis)))

#_(defn nd-aggregate
  ;; for when reduce-axis is brought online
  ([f t]
   (nd-aggregate (-> t rank first dec) f t))
  ([axis f t]
   (reduce-axis f t (or axis (-> t rank first dec)))))

(defn nd-reduce
  ([f t] (ndreduce f t 0))
  ([axis f t] (ndreduce axis f t)))

["Example of reducing along the 0th dimension"]

["APL reference:

```apl
      draw m
┏→━━━━━━━━━━━━━┓
↓ 0  1  2  3  4┃
┃ 5  6  7  8  9┃
┃10 11 12 13 14┃
┃              ┃
┃15 16 17 18 19┃
┃20 21 22 23 24┃
┃25 26 27 28 29┃
┗━━━━━━━━━━━━━━┛
 
```

Our version:"]

(def m t)
m




["APL reference:

```apl 
      draw +/[0] m
┏→━━━━━━━━━━━━━┓
↓15 17 19 21 23┃
┃25 27 29 31 33┃
┃35 37 39 41 43┃
┗━━━━━━━━━━━━━━┛

```

Which is equivalent let: "]
(let [f +] 
  (dtt/->tensor 
   [[(f (dtt/mget m 0 0 0) (dtt/mget m 1 0 0)) (f (dtt/mget m 0 0 1) (dtt/mget m 1 0 1)) (f (dtt/mget m 0 0 2) (dtt/mget m 1 0 2)) (f (dtt/mget m 0 0 3) (dtt/mget m 1 0 3)) (f (dtt/mget m 0 0 4) (dtt/mget m 1 0 4)) ]
    [(f (dtt/mget m 0 1 0) (dtt/mget m 1 1 0)) (f (dtt/mget m 0 1 1) (dtt/mget m 1 1 1)) (f (dtt/mget m 0 1 2) (dtt/mget m 1 1 2)) (f (dtt/mget m 0 1 3) (dtt/mget m 1 1 3)) (f (dtt/mget m 0 1 4) (dtt/mget m 1 1 4)) ]
    [(f (dtt/mget m 0 2 0) (dtt/mget m 1 2 0)) (f (dtt/mget m 0 2 1) (dtt/mget m 1 2 1)) (f (dtt/mget m 0 2 2) (dtt/mget m 1 2 2)) (f (dtt/mget m 0 2 3) (dtt/mget m 1 2 3)) (f (dtt/mget m 0 2 4) (dtt/mget m 1 2 4)) ]]))

["Which is equivalent to: "]

(nd-reduce 0 + m)

["Note that the initial shape of `m` is `[2 3 5]` and that `(first  m)` is 2.
Therefore when you reduce along axis 0, you can reason that the output shape will
be [3 5]."]

(dtype/shape m)
(dtype/shape (nd-reduce 0 + m))

["As for reducing along the first dimension: 


APL reference: 
```APL
      draw +/[1] m
┏→━━━━━━━━━━━━━┓
↓15 18 21 24 27┃
┃60 63 66 69 72┃
┗━━━━━━━━━━━━━━┛
```

Our version: "]

(nd-reduce 1 + m)

["Again, the shape of `m` is [2 3 5]`.  The index 1 of the shape of `m` is `3`.  Therefore, 
when we reduce along the 1 dimension of `m`, we can reason that the output shape will be 
[2 5]. Observe:"]

(dtype/shape (nd-reduce 1 + m))

["And finally for reducing along the last dimension:

APL reference:

```apl
      draw +/[2] m
┏→━━━━━━━━━┓
↓10  35  60┃
┃85 110 135┃
┗━━━━━━━━━━┛
```

"]

(nd-reduce 2 + m)

["Once more, the shape of `m` is [2 3 5].  Since the 2 dimension is 5, we can
reason that reducing the 2 dimension will result in an output shape of [2 3]: "]

(dtype/shape (nd-reduce 2 + m))

;; todo -- multixis reduce

["### Minimum and Maximum

`np.min(big_array), np.max(big_array)`"]

'tech.v3.datatype.functional/min
'tech.v3.datatype.functional/max

["#### Multi dimensional aggregates

One common type of aggregation operation is an aggregate along a row or column. Say you have some data stored in a two-dimensional array.

Aggregation functions take an additional argument specifying the axis along which the aggregate is computed. For example, we can find the minimum value within each column by specifying `axis=0`:

```
M.min(axis=0)
=> array([ 0.66859307,  0.03783739,  0.19544769,  0.06682827])
```"]

["#### Other aggregation functions

NumPy provides many other aggregation functions, but we won't discuss them in detail here. Additionally, most aggregates have a NaN-safe counterpart that computes the result while ignoring missing values, which are marked by the special IEEE floating-point NaN value (for a fuller discussion of missing data, see Handling Missing Data).

The following table provides a list of useful aggregation functions available in NumPy:

```
Function Name 	NaN-safe Version 	Description
np.sum 	np.nansum 	Compute sum of elements
np.prod 	np.nanprod 	Compute product of elements
np.mean 	np.nanmean 	Compute mean of elements
np.std 	np.nanstd 	Compute standard deviation
np.var 	np.nanvar 	Compute variance
np.min 	np.nanmin 	Find minimum value
np.max 	np.nanmax 	Find maximum value
np.argmin 	np.nanargmin 	Find index of minimum value
np.argmax 	np.nanargmax 	Find index of maximum value
np.median 	np.nanmedian 	Compute median of elements
np.percentile 	np.nanpercentile 	Compute rank-based statistics of elements
np.any 	N/A 	Evaluate whether any elements are true
np.all 	N/A 	Evaluate whether all elements are true
```
"]

["### Example: What is the Average Height of US Presidents?

```
import pandas as pd
data = pd.read_csv('data/president_heights.csv')
heights = np.array(data['height(cm)'])
print(\"Mean height:       \", heights.mean())
print(\"Standard deviation\", heights.std())
print(\"Minimum height:    \", heights.min())
print(\"Maximum height:    \", heights.max())

print(\"25th percentile:   \", np.percentile(heights, 25))
print(\"Median:            \", np.median(heights))
print(\"75th percentile:   \", np.percentile(heights, 75))

%matplotlib inline
import matplotlib.pyplot as plt
import seaborn; seaborn.set()  # set plot style
plt.hist(heights)
plt.title('Height Distribution of US Presidents')
plt.xlabel('height (cm)')
plt.ylabel('number'));
```"]

(def csv-data (ds/->dataset "https://raw.githubusercontent.com/jakevdp/PythonDataScienceHandbook/master/notebooks/data/president_heights.csv"))
^k/dataset
(ds/head csv-data 3)

(tech.v3.datatype.functional/mean (dtype/->reader (-> csv-data last last)))

["Computation on Arrays: Broadcasting
 ------------------------------------------------"]

(defn broadcast [f t]
  (dtype/emap f :object t))

(broadcast zero? (eye 5))


["### WIP"]

["Comparisons, Masks, and Boolean Logic
 ------------------------------------------------"]

(defn mask [f t]
  (-> (dtype/emap #(if (f %) 1 0) :int32 t)
      dtt/->tensor
      ravel))

(mask zero? (eye 5))

["Fancy Indexing
 ------------------------------------------------"]


(defn compress
  "Use compress to do an elementwise selection according to an interger boolean
mask. lhs and rhs must have the same element count.  A 0 on the lhs will drop the corresponding
element on the rhs.  A 1 on the rhs will keep the element. 2 or great will cause replication
of the element on the rhs. Returns a tensor of rank 1."
  [lhs rhs]
  (let [rhs   (dtt/->tensor rhs)
        rhr   (dtt/->tensor (ravel rhs))
        dt    (dtype/get-datatype rhs)
        shape (dtype/shape lhs)
        idx'  (into []
                    (comp
                     (partition-all 2)
                     (map #(repeat (first %) (second %)))
                     cat)
                    (interleave lhs (index shape)))
        size  (count idx')]
    (-> (dtype/make-reader :object size (apply dtt/mget rhr (nth idx' idx)))
        (dtt/->tensor))))

(compress [0 0 1 0 2] [1 1 2 3 4])


["Sorting Arrays
 ------------------------------------------------"]



["Structured Data: NumPy's Structured Arrays
  ------------------------------------------------"]
