(ns scicloj.03-data-manipulation.00-data-manipulation
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

["# Data Manipulation with tablecloth"]

["In the previous chapter, we dove into detail on NumPy and its ndarray object,
which provides efficient storage and manipulation of dense typed arrays in
Python. Here we'll build on this knowledge by looking in detail at the data
structures provided by the Pandas library. Pandas is a newer package built on
top of NumPy, and provides an efficient implementation of a
DataFrame. DataFrames are essentially multidimensional arrays with attached row
and column labels, and often with heterogeneous types and/or missing data. As
well as offering a convenient storage interface for labeled data, Pandas
implements a number of powerful data operations familiar to users of both
database frameworks and spreadsheet programs.

As we saw, NumPy's ndarray data structure provides essential features for the
type of clean, well-organized data typically seen in numerical computing
tasks. While it serves this purpose very well, its limitations become clear when
we need more flexibility (e.g., attaching labels to data, working with missing
data, etc.) and when attempting operations that do not map well to element-wise
broadcasting (e.g., groupings, pivots, etc.), each of which is an important
piece of analyzing the less structured data available in many forms in the world
around us. Pandas, and in particular its Series and DataFrame objects, builds on
the NumPy array structure and provides efficient access to these sorts of \"data
munging\" tasks that occupy much of a data scientist's time.

In this chapter, we will focus on the mechanics of using Series, DataFrame, and
related structures effectively. We will use examples drawn from real datasets
where appropriate, but these examples are not necessarily the focus."]

["## Installing and Using tablecloth"]

["Installation of Pandas on your system requires NumPy to be installed, and if
building the library from source, requires the appropriate tools to compile the
C and Cython sources on which Pandas is built. Details on this installation can
be found in the Pandas documentation. If you followed the advice outlined in the
Preface and used the Anaconda stack, you already have Pandas installed."]

["Once Pandas is installed, you can import it and check the version:"]

(require 'tablecloth.api)

;; TODO: How to check tablecloth version?

["Just as we generally import NumPy under the alias np, we will import tablecloth
under the alias tablecloth:"]

(require '[tablecloth.api :as tablecloth])

["This require convention will be used throughout the remainder of this book."]

["## Reminder about Built-In Documentation"]

["As you read through this chapter, don't forget that clojure and cider gives
you the ability to quickly explore the contents of a namespace (by using the
tab-completion feature) as well as the documentation of various functions (using
`doc` and `find-doc`)."]

["For example, to display all the contents of the tablecloth namespace, you can
type:

`tablecloth/<TAB>`"]

["And to display tablecloth's built-in documentation, you can use this:

`(clojure.repl/doc tablecloth.api)`"]

(clojure.repl/doc tablecloth.api)

["To display document of a function:"]

(clojure.repl/doc tablecloth/column-names)

["More detailed documentation, along with tutorials and other resources, can be
found at https://scicloj.github.io/tablecloth/index.html."]
