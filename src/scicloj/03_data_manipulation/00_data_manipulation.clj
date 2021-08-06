(ns scicloj.03-data-manipulation.00-data-manipulation
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

["# Data Manipulation with tablecloth"]

["In the previous chapter, we dove into detail on dataset, which provides
efficient storage and manipulation of dense typed arrays in clojure. Here we'll
build on this knowledge by looking in detail at the data structures provided by
the dataset/tablecloth library. Tablecloth is a well defined API on top of
dataset, and provides an efficient implementation of a dataset. Datasets are
essentially multidimensional arrays with attached column labels, and often with
heterogeneous types and/or missing data. As well as offering a convenient
storage interface for labeled data, Tablecloth implements a number of powerful
data operations familiar to users of both database frameworks and spreadsheet
programs.

As we saw, Clojure's vector data structure provides essential features for the
type of clean, well-organized data typically seen in numerical computing
tasks. While it serves this purpose very well, its limitations become clear when
we need more flexibility (e.g., attaching labels to data, working with missing
data, etc.) and when attempting operations that do not map well to element-wise
broadcasting (e.g., groupings, pivots, etc.), each of which is an important
piece of analyzing the less structured data available in many forms in the world
around us. Tablecloth, and in particular its Column and Dataset objects, builds on
the clojure vector? structure and provides efficient access to these sorts of \"data
munging\" tasks that occupy much of a data scientist's time.

In this chapter, we will focus on the mechanics of using Columns, Dataset, and
related structures effectively. We will use examples drawn from real datasets
where appropriate, but these examples are not necessarily the focus."]

["## Installing and Using tablecloth"]

["Installation of tablecloth on your system is as easy as just add the deps and
require it in code."]

["Once tablecloth is add to deps.edn, you can require it and check the
version:"]

^kind/naive
(get-in (read-string (slurp "deps.edn"))
        [:deps 'scicloj/tablecloth :mvn/version])

;; TODO: shall we put tablecloth version info to API interface?
;; for example: tablecloth.api/version
;; even better that we can check upstream tech.ml.dataset version with:
;; tablecloth.api/dataset-version

["Just as we generally require clojure namespace under a alias, we will import
tablecloth.api under the alias tablecloth:"]

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
`(clojure.repl/doc tablecloth.api)` and switch to nrepl to check the
printouts."]

(clojure.repl/doc tablecloth.api)

["To display document of a function:"]

(clojure.repl/doc tablecloth/column-names)

["More detailed documentation, along with tutorials and other resources, can be
found at https://scicloj.github.io/tablecloth/index.html."]
