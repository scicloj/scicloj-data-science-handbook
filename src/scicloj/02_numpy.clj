(ns scicloj.02-numpy
  (:require
    [notespace.api]
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
Python Data Science - ch.2. NumPy translated to Clojure
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
 '[tech.v3.datatype.functional :as dtfn]
 '[tech.v3.tensor :as dtt]
 '[tablecloth.api :as api])

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

(defn outer-product [f a b]
  (let [final-shape (concat (dtype/shape a) (dtype/shape b))
        size        (apply * final-shape)
        index-a     (index (dtype/shape a))
        index-b     (index (dtype/shape b))
        indexes     (for [a index-a
                          b index-b]
                      [a b])]
    (-> (dtype/make-reader
         :object
         size
         (let [[a' b'] (nth indexes idx)]
           (f (apply dtt/mget a a')
              (apply dtt/mget b b'))))
        (dtt/->tensor)
        (dtt/reshape final-shape))))

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

(defn ravel [t]
  (let [dt    (dtype/get-datatype t)
        shape (dtype/shape t)
        idx'  (index shape)]
    (-> (dtype/make-reader dt (apply * shape) (apply dtt/mget t (nth idx' idx)))
        (dtt/->tensor))))
(dtype/get-datatype x)
(dtype/shape x)
(index (dtype/shape x))
(ravel x)

(defn mask [f t]
  (-> (dtype/emap #(if (f %) 1 0) :int32 t)
      dtt/->tensor
      ravel))

(mask #(= 1 %) x)


(defn compress [lhs rhs]
  (let [lhr   (ravel lhs)
        rhr   (ravel rhs)
        dt    (dtype/get-datatype rhs)
        shape (dtype/shape lhs)
        idx'  (into []
                    (comp
                     (partition-all 2)
                     (map #(repeat (first %) (second %)))
                     cat)
                    (interleave (ravel lhs) (index shape)))
        size  (count idx')]
    (-> (dtype/make-reader dt size (apply dtt/mget rhr (nth idx' idx)))
        (dtt/->tensor))))



(compress (mask (partial = 1) x) x)


(defn ^:shameful start-stop-step-reverse [start stop step]
  (def start start)
  (def stop stop)
  (def step step)
  (cond
    (and start stop step (pos? step)) ;; x[1:10:2]
    [start stop step false]
    (and start stop (nil? step)) ;; x[1:10]
    [start stop 1 false]
    (and start stop step (neg? step)) ;; x[1:10:-1]
    [stop start (Math/abs step) true]
    (and start (nil? stop) step (pos? step)) ;; x[1::2]
    [start (first (dtype/shape t)) step false]
    (and start (nil? stop) (nil? step)) ;; x[1::]
    [start (first (dtype/shape t)) 1 false]
    (and start (nil? stop) step (neg? step)) ;; x[1::-2]
    [0 start (Math/abs step) true]
    (and (nil? start) stop step (pos? step)) ;; x[:5:2]
    [0 stop step false]
    (and (nil? start) stop (nil? step)) ;; x[:5]
    [0 stop 1 false]
    (and (nil? start) stop step (neg? step)) ;; x[:5:-2]
    [stop (dec (first (dtype/shape t))) (Math/abs step) true]
    (and (nil? start) (nil? stop) step (pos? step)) ;; x[::2]
    [0 (first (dtype/shape t)) step false]
    (and (nil? start) (nil? stop) (nil? step)) ;; x[::]
    [0 (first (dtype/shape t)) 1 false]
    (and (nil? start) (nil? stop) step (neg? step)) ;; x[::-2]
    [(dec (first (dtype/shape t))) 0 (Math/abs step) true]))


(comment
  
  (start-stop-step-reverse 10 20 nil)

  ,)
(defn
  ^{:doc
    "API mostly matches API of numpy, except for some corner cases,
and an off-by-one error on some negative steps. 
This is the most horrible thing I've ever written.  Someone please help. :(
--JJ"}
  array-slice
  ([t stop] (array-slice t nil stop nil))
  ([t start stop] (array-slice t start stop nil))
  ([t start stop step]
   (do (ns-unmap *ns* 'start')
       (ns-unmap *ns* 'stop')
       (ns-unmap *ns* 'step')
       (ns-unmap *ns* 'reverse?'))
   (let [[start stop step reverse?]
         (start-stop-step-reverse start stop step)
         _ (def start' start)
         _ (def stop' stop)
         _ (def step' step)
         _ (def reverse?' reverse?)
         upper-bound (partial > stop)
         lower-bound (partial <= start)
         modulus     (partial mod (Math/abs step))
         bounds      (mask #(and (upper-bound %) (lower-bound %)) t)
         shape       (dtype/shape bounds)
         idx'        (transduce (take-nth (Math/abs step))
                                (fn ([res]
                                     (if reverse?
                                       (reverse (persistent! res))
                                       (persistent! res)))
                                  ([res next] (conj! res next)))
                                (transient [])
                                (index shape))]
     
     
     (when (not-empty idx')
       (->> (dtype/make-reader :object
                               (count idx')
                               (apply dtt/mget t (nth idx' idx)))
            (dtt/->tensor))))))

["These work as expected"]
(array-slice x 0 20 2)
(array-slice x nil 20 2)
(array-slice x nil nil 2)
(array-slice x 0 nil 2)

(array-slice x 20 0 -2)
(array-slice x 20 nil -2)
(array-slice x nil 0 -2)
(array-slice x nil nil -2)

(start-stop-step-reverse 0 20 nil)
(array-slice 0 20 nil)




(array-slice x 0 nil -2)
different
(array-slice x 20 nil -2)

(array-slice x 5 nil -1)





["### Multi-dimensional subarrays

Same as for 1D arrays.

`x2[:2, :3]  # two rows, three columns`

Clj: You can get/set subrects at a given time using mget/mset! pathways from `tech.v3.tensor`."]

["#### Accessing array rows and columns

One commonly needed routine is accessing of single rows or columns of an array.

`print(x2[:, 0])  # first column of x2`
`print(x2[0, :])  # first row of x2`"]

["#### Subarrays as no-copy views

views rather than copies of the array data

NOTE: You can use `tech.v3.datatype.argops` to create \"indexes\" for a buffer and then
combine these with the buffer using `tech.v3.datatype/indexed-buffer` to create a custom
view of the original buffer"]

["#### Creating copies of arrays

Despite the nice features of array views, it is sometimes useful to instead explicitly copy the data within an array or a subarray."]

;; see "Copy" in https://cnuernber.github.io/dtype-next/cheatsheet.html
;; see https://cnuernber.github.io/dtype-next/tech.v3.datatype.html#var-copy.21

["### Reshaping of Arrays

For example, if you want to put the numbers 1 through 9 in a 3×3 grid"]

;; see https://scicloj.github.io/tablecloth/#Reshape and ... ?


["### Array Concatenation and Splitting

All of the preceding routines worked on single arrays. It's also possible to combine multiple arrays into one, and to conversely split a single array into multiple arrays. "]

;; see https://scicloj.github.io/tablecloth/#JoinConcat_Datasets
;; see https://cnuernber.github.io/dtype-next/tech.v3.datatype.html#var-concat-buffers

["#### Concatenation of arrays

NumPy: `np.concatenate, np.vstack, and np.hstack`"]


["#### Splitting of arrays

The opposite of concatenation is splitting, which is implemented by the functions `np.split`, `np.hsplit`, `np.vsplit`, and `np.dsplit` [depth?]. For each of these, we can pass a list of indices giving the split points:"]

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


["### Exploring NumPy's UFuncs"]
["#### Array arithmetic

* addition, subtraction, multiplication, and division (+ `floor_divide`).
* unary ufunc for negation, and a ** operator for exponentiation, and a % operator for modulus

these can be strung together however you wish, and the standard order of operations is respecte"]


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

["#### Outer products

Finally, any ufunc can compute the output of all pairs of two different inputs using the `outer` method. This allows you, in one line, to do things like create a multiplication table:

```
x = np.arange(1, 6)
np.multiply.outer(x, x)
```"]

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

["### Minimum and Maximum

`np.min(big_array), np.max(big_array)`"]

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

["Computation on Arrays: Broadcasting
 ------------------------------------------------"]

["### WIP"]

["Comparisons, Masks, and Boolean Logic
 ------------------------------------------------"]

["Fancy Indexing
 ------------------------------------------------"]

["Sorting Arrays
 ------------------------------------------------"]

["Structured Data: NumPy's Structured Arrays
  ------------------------------------------------"]
