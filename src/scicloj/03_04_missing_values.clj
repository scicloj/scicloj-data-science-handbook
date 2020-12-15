(ns scicloj.03-04-missing-values
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
  ;; Generate static html
  (notespace/render-static-html (str "docs/scicloj/ch03/"
                                     (clojure.string/replace
                                      (last (clojure.string/split (str *ns*) #"\."))
                                      "-" "_")
                                     ".html")))

["# Handling Missing Data"]

["The difference between data found in many tutorials and data in the real world
is that real-world data is rarely clean and homogeneous. In particular, many
interesting datasets will have some amount of data missing. To make matters even
more complicated, different data sources may indicate missing data in different
ways.

In this section, we will discuss some general considerations for missing data,
discuss how Pandas chooses to represent it, and demonstrate some built-in Pandas
tools for handling missing data in Python. Here and throughout the book, we'll
refer to missing data in general as null, NaN, or NA values."]

["## Trade-Offs in Missing Data Conventions"]

["There are a number of schemes that have been developed to indicate the
presence of missing data in a table or DataFrame. Generally, they revolve around
one of two strategies: using a mask that globally indicates missing values, or
choosing a sentinel value that indicates a missing entry.

In the masking approach, the mask might be an entirely separate Boolean array,
or it may involve appropriation of one bit in the data representation to locally
indicate the null status of a value.

In the sentinel approach, the sentinel value could be some data-specific
convention, such as indicating a missing integer value with -9999 or some rare
bit pattern, or it could be a more global convention, such as indicating a
missing floating-point value with NaN (Not a Number), a special value which is
part of the IEEE floating-point specification.

None of these approaches is without trade-offs: use of a separate mask array
requires allocation of an additional Boolean array, which adds overhead in both
storage and computation. A sentinel value reduces the range of valid values that
can be represented, and may require extra (often non-optimized) logic in CPU and
GPU arithmetic. Common special values like NaN are not available for all data
types.

As in most cases where no universally optimal choice exists, different languages
and systems use different conventions. For example, the R language uses reserved
bit patterns within each data type as sentinel values indicating missing data,
while the SciDB system uses an extra byte attached to every cell which indicates
a NA state."]

["## Missing Data in Dataset"]

["The way in which Pandas handles missing values is constrained by its reliance
on the NumPy package, which does not have a built-in notion of NA values for
non-floating-point data types.

Pandas could have followed R's lead in specifying bit patterns for each
individual data type to indicate nullness, but this approach turns out to be
rather unwieldy. While R contains four basic data types, NumPy supports far more
than this: for example, while R has a single integer type, NumPy supports
fourteen basic integer types once you account for available precisions,
signedness, and endianness of the encoding. Reserving a specific bit pattern in
all available NumPy types would lead to an unwieldy amount of overhead in
special-casing various operations for various types, likely even requiring a new
fork of the NumPy package. Further, for the smaller data types (such as 8-bit
integers), sacrificing a bit to use as a mask will significantly reduce the
range of values it can represent.

NumPy does have support for masked arrays – that is, arrays that have a separate
Boolean mask array attached for marking data as \"good\" or \"bad\". Pandas
could have derived from this, but the overhead in both storage, computation, and
code maintenance makes that an unattractive choice.

With these constraints in mind, Pandas chose to use sentinels for missing data,
and further chose to use two already-existing Python null values: the special
floating-point NaN value, and the Python None object. This choice has some side
effects, as we will see, but in practice ends up being a good compromise in most
cases of interest."]

["### None: Clojure missing data"]

["The first sentinel value used by Pandas is None, a Python singleton object
that is often used for missing data in Python code. Because it is a Python
object, None cannot be used in any arbitrary NumPy/Pandas array, but only in
arrays with data type 'object' (i.e., arrays of Python objects):"]

(require '[tablecloth.api :as tablecloth]
         '[tech.v3.dataset :as ds]
         '[tech.v3.datatype.functional :as dfn])

(def vals1 (tablecloth/dataset {:A [1 nil 3 4]}))

^kind/dataset
vals1

(ds/brief vals1)

["It is different from Pandas dataframe that missing value is just ignored in
tech.ml.dataset:"]

(dfn/sum (vals1 :A))

["## Operating on Missing Values"]

["### Detecting null values"]

["tech.ml.dataset data structures have two useful methods for detecting null data:
`missing`. Either one will return a Boolean mask over the data. For example:"]

(def data (tablecloth/dataset {:A [1 "hello" nil]}))

(ds/missing data)

["### Dropping null values"]

["In addition to the masking used before, there are the convenience methods,
`drop-missing` (which removes NA values) and `replace-missing` (which fills in
NA values). For a Series, the result is straightforward:"]

(tablecloth/drop-missing data)

["For a DataFrame, there are more options. Consider the following DataFrame:"]

(def DS (tablecloth/dataset {:A [1 2 nil]
                             :B [nil 3 4]
                             :C [2 5 6]}))
^kind/dataset
DS

["We cannot drop single values from a DataFrame; we can only drop full rows or
full columns. Depending on the application, you might want one or the other, so
dropna() gives a number of options for a DataFrame.

By default, dropna() will drop all rows in which any null value is present:"]

(tablecloth/drop-missing DS)

["Alternatively, you can drop NA values along a different axis; axis=1 drops all
columns containing a null value:"]

(tablecloth/drop-missing DS :A)

["But this drops some good data as well; you might rather be interested in
dropping rows or columns with all NA values, or a majority of NA values. This
can be specified through the how or thresh parameters, which allow fine control
of the number of nulls to allow through.

The default is how='any', such that any row or column (depending on the axis
keyword) containing a null value will be dropped. You can also specify
how='all', which will only drop rows/columns that are all null values:"]

(require '[tech.v3.dataset.column-filters :as cf]
         '[tech.v3.dataset.column :as ds-col])

(def DS1 (tablecloth/add-or-replace-column DS :D nil))
^kind/dataset
DS1

^kind/dataset
(tablecloth/drop-columns DS1 (tablecloth/column-names
                              (cf/column-filter DS1 #(= (ds/row-count DS1) (count (vec (ds-col/missing %)))))))

["For finer-grained control, the thresh parameter lets you specify a minimum
number of non-null values for the row/column to be kept:"]

^kind/dataset
(tablecloth/drop-columns DS1 (tablecloth/column-names
                              (cf/column-filter DS1 #(= 3  (count (vec (ds-col/missing %)))))))

^kind/dataset
(tablecloth/drop-rows DS1 (fn [row]
                            (> 3 (count (filter some? (vals row))))))

["Here the first and last row have been dropped, because they contain only two
non-null values."]

["### Filling null values"]

["Sometimes rather than dropping NA values, you'd rather replace them with a
valid value. This value might be a single number like zero, or it might be some
sort of imputation or interpolation from the good values. You could do this
in-place using the isnull() method as a mask, but because it is such a common
operation tech.ml.dataset provides the `replace-missing` function, which returns
a copy of the array with the null values replaced."]

["Consider the following dataset:"]

(def DS2 (tablecloth/dataset {:A [1 nil 2 nil 3]}))
^kind/dataset
DS2

["We can fill NA entries with a single value, such as zero:"]

^kind/dataset
(tablecloth/replace-missing DS2 [:A] :value 0)

["We can specify a down-fill to propagate the previous value down:"]

^kind/dataset
(tablecloth/replace-missing DS2 [:A] :down)

["Or we can specify a up-fill to propagate the next values up:"]

^kind/dataset
(tablecloth/replace-missing DS2 [:A] :up)

["Or we can specify a mid-fill to calculate the missing value:"]

^kind/dataset
(tablecloth/replace-missing DS2 [:A] :mid)

["Or we can specify a Linear interpolation fill to calculate the missing value:"]

^kind/dataset
(tablecloth/replace-missing DS2 [:A] :lerp)

["Notice that if a previous value is not available during a down fill, the up
value are used."]
