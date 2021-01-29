(ns scicloj.03-data-manipulation.08-aggregation-and-grouping
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

["# Aggregation and Grouping"]

["An essential piece of analysis of large data is efficient summarization:
computing aggregations like sum(), mean(), median(), min(), and max(), in which
a single number gives insight into the nature of a potentially large dataset. In
this section, we'll explore aggregations in Pandas, from simple operations akin
to what we've seen on NumPy arrays, to more sophisticated operations based on
the concept of a groupby.

For convenience, we'll use the same display magic function that we've seen in
previous sections:"]

;; TODO

["## Planets Data"]

["Here we will use the Planets dataset, available via the Seaborn package (see
Visualization With Seaborn). It gives information on planets that astronomers
have discovered around other stars (known as extrasolar planets or exoplanets
for short). It can be downloaded with a simple Seaborn command:"]

(require '[tablecloth.api :as tablecloth])

(def planets (tablecloth/dataset "https://raw.githubusercontent.com/mwaskom/seaborn-data/master/planets.csv"))

^kind/naive
(tablecloth/shape planets)

^kind/dataset-grid
(tablecloth/head planets)

["This has some details on the 1,000+ extrasolar planets discovered up to
2014."]

["## Simple Aggregation in Pandas"]

["Earlier, we explored some of the data aggregations available for NumPy
arrays (Aggregations: Min, Max, and Everything In Between). As with a
one-dimensional NumPy array, for a Pandas Series the aggregates return a single
value:
```
rng = np.random.RandomState(42)
ser = pd.Series(rng.rand(5))
ser
ser.sum()
ser.mean()
```"]

;; Column?

["For a DataFrame, by default the aggregates return results within each
column:"]

(def DS (tablecloth/dataset {:A (repeatedly 5 rand)
                             :B (repeatedly 5 rand)}))

^kind/dataset-grid
DS

(require '[tech.v3.datatype.functional :as dfn])

^kind/dataset-grid
(tablecloth/aggregate-columns DS :all dfn/sum)

^kind/dataset-grid
(tablecloth/aggregate-columns DS :all dfn/mean)

["By specifying the axis argument, you can instead aggregate within each row:"]

(require '[tech.v3.dataset :as dataset])

;; TODO

["Pandas Series and DataFrames include all of the common aggregates mentioned in
Aggregations: Min, Max, and Everything In Between; in addition, there is a
convenience method describe() that computes several common aggregates for each
column and returns the result. Let's use this on the Planets data, for now
dropping rows with missing values:"]

^kind/dataset-grid
(->  planets
     tablecloth/drop-missing
     dataset/descriptive-stats)

["This can be a useful way to begin understanding the overall properties of a
dataset. For example, we see in the year column that although exoplanets were
discovered as far back as 1989, half of all known expolanets were not discovered
until 2010 or after. This is largely thanks to the Kepler mission, which is a
space-based telescope specifically designed for finding eclipsing planets around
other stars."]

["The following table summarizes some other built-in Pandas aggregations:

Aggregation 	Description
count() 	Total number of items
first(), last() 	First and last item
mean(), median() 	Mean and median
min(), max() 	Minimum and maximum
std(), var() 	Standard deviation and variance
mad() 	Mean absolute deviation
prod() 	Product of all items
sum() 	Sum of all items

These are all methods of DataFrame and Series objects.

To go deeper into the data, however, simple aggregates are often not enough. The
next level of data summarization is the groupby operation, which allows you to
quickly and efficiently compute aggregates on subsets of data."]

["## GroupBy: Split, Apply, Combine"]
