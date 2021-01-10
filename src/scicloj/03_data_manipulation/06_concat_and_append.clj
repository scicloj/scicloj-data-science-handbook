(ns scicloj.03-data-manipulation.06-concat-and-append
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
  (notespace/render-static-html))

["# Combining Datasets: Concat and Append"]

["Some of the most interesting studies of data come from combining different data
sources. These operations can involve anything from very straightforward
concatenation of two different datasets, to more complicated database-style
joins and merges that correctly handle any overlaps between the datasets. Series
and DataFrames are built with this type of operation in mind, and Pandas
includes functions and methods that make this sort of data wrangling fast and
straightforward.

Here we'll take a look at simple concatenation of Series and DataFrames with the
pd.concat function; later we'll dive into more sophisticated in-memory merges
and joins implemented in Pandas.

We begin with the standard imports:"]

(require '[tablecloth.api :as tablecloth])

["For convenience, we'll define this function which creates a DataFrame of a
particular form that will be useful below:"]

(defn make-ds
  "Quickly make a dataset"
  [cols rows]
  (let [data (into {}
                   (for [c cols]
                     {(str c) (map #(str c %) rows)}))]
    (tablecloth/dataset data)))

^kind/dataset
(make-ds "ABC" (range 3))

["In addition, we'll create a quick class that allows us to display multiple
DataFrames side by side. The code makes use of the special _repr_html_ method,
which IPython uses to implement its rich object display:"]

;; TODO: use hiccup to display multiple dataset

["The use of this will become clearer as we continue our discussion in the
following section."]

["## Recall: Concatenation of NumPy Arrays"]

["Concatenation of Series and DataFrame objects is very similar to concatenation
of Numpy arrays, which can be done via the np.concatenate function as discussed
in The Basics of NumPy Arrays. Recall that with it, you can combine the contents
of two or more arrays into a single array:"]

;; TODO: tensor from chapter 2

["The first argument is a list or tuple of arrays to concatenate. Additionally,
it takes an axis keyword that allows you to specify the axis along which the
result will be concatenated:"]

;; TODO: tensor concat by axis from chapter 2

["## Simple Concatenation with tablecloth dataset"]

["Pandas has a function, pd.concat(), which has a similar syntax to
np.concatenate but contains a number of options that we'll discuss
momentarily:"]

["pd.concat() can be used for a simple concatenation of Series or DataFrame
objects, just as np.concatenate() can be used for simple concatenations of
arrays:"]

["It also works to concatenate higher-dimensional objects, such as DataFrames:"]

(def ds1 (make-ds "AB" [1 2]))
(def ds2 (make-ds "AB" [3 4]))
^kind/dataset
ds1
^kind/dataset
ds2
^kind/dataset
(tablecloth/concat ds1 ds2)

["By default, the concatenation takes place row-wise within the DataFrame (i.e.,
axis=0). Like np.concatenate, pd.concat allows specification of an axis along
which concatenation will take place. Consider the following example:"]

(def ds3 (make-ds "AB" [0 1]))
(def ds4 (make-ds "CD" [0 1]))
^kind/dataset
ds3
^kind/dataset
ds4
^kind/dataset
(tablecloth/append ds3 ds4)

["We could have equivalently specified axis=1; here we've used the more
intuitive axis='col'."]

["### Duplicate indices"]

["TODO: skip row index secsion, as dataset do not support row index"]

["### Concatenation with joins"]

["In the simple examples we just looked at, we were mainly concatenating
DataFrames with shared column names. In practice, data from different sources
might have different sets of column names, and pd.concat offers several options
in this case. Consider the concatenation of the following two DataFrames, which
have some (but not all!) columns in common:"]

(def ds5 (make-ds "ABC" [1 2]))
(def ds6 (make-ds "BCD" [3 4]))
;; TODO

["By default, the entries for which no data is available are filled with NA
values. To change this, we can specify one of several options for the join and
join_axes parameters of the concatenate function. By default, the join is a
union of the input columns (join='outer'), but we can change this to an
intersection of the columns using join='inner':"]

(tablecloth/inner-join ds5 ds6 ["B" "C"])

["Another option is to directly specify the index of the remaininig colums using
the join_axes argument, which takes a list of index objects. Here we'll specify
that the returned columns should be the same as those of the first input:

```
display('df5', 'df6',
        \"pd.concat([df5, df6], join_axes=[df5.columns])\")
```"]

["The combination of options of the pd.concat function allows a wide range of
possible behaviors when joining two datasets; keep these in mind as you use
these tools for your own data."]
