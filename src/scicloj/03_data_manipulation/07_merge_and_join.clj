(ns scicloj.03-data-manipulation.07-merge-and-join
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

["# Combining Datasets: Merge and Join"]

["One essential feature offered by Pandas is its high-performance, in-memory
join and merge operations. If you have ever worked with databases, you should be
familiar with this type of data interaction. The main interface for this is the
pd.merge function, and we'll see few examples of how this can work in practice.

For convenience, we will start by redefining the display() functionality from
the previous section:"]

;; TODO

(require '[tablecloth.api :as tablecloth])

["## Relational Algebra"]

["The behavior implemented in pd.merge() is a subset of what is known as
relational algebra, which is a formal set of rules for manipulating relational
data, and forms the conceptual foundation of operations available in most
databases. The strength of the relational algebra approach is that it proposes
several primitive operations, which become the building blocks of more
complicated operations on any dataset. With this lexicon of fundamental
operations implemented efficiently in a database or other program, a wide range
of fairly complicated composite operations can be performed.

Pandas implements several of these fundamental building-blocks in the pd.merge()
function and the related join() method of Series and Dataframes. As we will see,
these let you efficiently link data from different sources."]

["## Categories of Joins"]

["The pd.merge() function implements a number of types of joins: the one-to-one,
many-to-one, and many-to-many joins. All three types of joins are accessed via
an identical call to the pd.merge() interface; the type of join performed
depends on the form of the input data. Here we will show simple examples of the
three types of merges, and discuss detailed options further below."]

["### One-to-one joins"]

["Perhaps the simplest type of merge expresion is the one-to-one join, which is
in many ways very similar to the column-wise concatenation seen in Combining
Datasets: Concat & Append. As a concrete example, consider the following two
DataFrames which contain information on several employees in a company:

```
df1 = pd.DataFrame({'employee': ['Bob', 'Jake', 'Lisa', 'Sue'],
                    'group': ['Accounting', 'Engineering', 'Engineering', 'HR']})
df2 = pd.DataFrame({'employee': ['Lisa', 'Bob', 'Jake', 'Sue'],
                    'hire_date': [2004, 2008, 2012, 2014]})
display('df1', 'df2')
```"]

(def ds1 (tablecloth/dataset {:employee ["Bob" "Jake" "Lisa" "Sue"]
                              :group ["Accounting" "Engineering" "Engineering" "HR"]}))
(def ds2 (tablecloth/dataset {:employee ["Lisa" "Bob" "Jake" "Sue"]
                              :hire-date [2004 2008 2012 2014]}))
^kind/dataset
ds1
;; => _unnamed [4 2]:
;;    | :employee |      :group |
;;    |-----------|-------------|
;;    |       Bob |  Accounting |
;;    |      Jake | Engineering |
;;    |      Lisa | Engineering |
;;    |       Sue |          HR |

^kind/dataset
ds2
;; => _unnamed [4 2]:
;;    | :employee | :hire-date |
;;    |-----------|------------|
;;    |      Lisa |       2004 |
;;    |       Bob |       2008 |
;;    |      Jake |       2012 |
;;    |       Sue |       2014 |

["To combine this information into a single DataFrame, we can use the pd.merge()
function:"]

(def ds3 (tablecloth/inner-join ds1 ds2 :employee))
^kind/dataset
ds3
;; => inner-join [4 3]:
;;    | :employee |      :group | :hire-date |
;;    |-----------|-------------|------------|
;;    |      Lisa | Engineering |       2004 |
;;    |       Bob |  Accounting |       2008 |
;;    |      Jake | Engineering |       2012 |
;;    |       Sue |          HR |       2014 |

["The pd.merge() function recognizes that each DataFrame has an \"employee\"
column, and automatically joins using this column as a key. The result of the
merge is a new DataFrame that combines the information from the two
inputs. Notice that the order of entries in each column is not necessarily
maintained: in this case, the order of the \"employee\" column differs between
df1 and df2, and the pd.merge() function correctly accounts for
this. Additionally, keep in mind that the merge in general discards the index,
except in the special case of merges by index (see the left_index and
right_index keywords, discussed momentarily)."]

["### Many-to-one joins"]

["Many-to-one joins are joins in which one of the two key columns contains
duplicate entries. For the many-to-one case, the resulting DataFrame will
preserve those duplicate entries as appropriate. Consider the following example
of a many-to-one join:"]

(def ds4 (tablecloth/dataset {:group ["Accounting" "Engineering" "HR"]
                              :supervisor ["Carly" "Guido" "Steve"]}))

^kind/dataset
ds3
;; => inner-join [4 3]:
;;    | :employee |      :group | :hire-date |
;;    |-----------|-------------|------------|
;;    |      Lisa | Engineering |       2004 |
;;    |       Bob |  Accounting |       2008 |
;;    |      Jake | Engineering |       2012 |
;;    |       Sue |          HR |       2014 |

^kind/dataset
ds4
;; => _unnamed [3 2]:
;;    |      :group | :supervisor |
;;    |-------------|-------------|
;;    |  Accounting |       Carly |
;;    | Engineering |       Guido |
;;    |          HR |       Steve |

^kind/dataset
(tablecloth/inner-join ds3 ds4 :group)
;; => inner-join [4 4]:
;;    |      :group | :employee | :hire-date | :supervisor |
;;    |-------------|-----------|------------|-------------|
;;    |  Accounting |       Bob |       2008 |       Carly |
;;    | Engineering |      Lisa |       2004 |       Guido |
;;    | Engineering |      Jake |       2012 |       Guido |
;;    |          HR |       Sue |       2014 |       Steve |

["The resulting DataFrame has an aditional column with the \"supervisor\"
information, where the information is repeated in one or more locations as
required by the inputs.
"]

["### Many-to-many joins"]

["Many-to-many joins are a bit confusing conceptually, but are nevertheless well
defined. If the key column in both the left and right array contains duplicates,
then the result is a many-to-many merge. This will be perhaps most clear with a
concrete example. Consider the following, where we have a DataFrame showing one
or more skills associated with a particular group. By performing a many-to-many
join, we can recover the skills associated with any individual person:"]

(def ds5 (tablecloth/dataset {:group ["Accounting" "Accounting" "Engineering" "Engineering" "HR" "HR"]
                              :skills ["math" "spreadsheets" "coding" "linux" "spreadsheets" "organization"]}))

ds1
;; => _unnamed [4 2]:
;;    | :employee |      :group |
;;    |-----------|-------------|
;;    |       Bob |  Accounting |
;;    |      Jake | Engineering |
;;    |      Lisa | Engineering |
;;    |       Sue |          HR |

ds5
;; => _unnamed [6 2]:
;;    |      :group |      :skills |
;;    |-------------|--------------|
;;    |  Accounting |         math |
;;    |  Accounting | spreadsheets |
;;    | Engineering |       coding |
;;    | Engineering |        linux |
;;    |          HR | spreadsheets |
;;    |          HR | organization |

^kind/dataset
(tablecloth/inner-join ds1 ds5 :group)
;; => inner-join [8 3]:
;;    |      :group | :employee |      :skills |
;;    |-------------|-----------|--------------|
;;    |  Accounting |       Bob |         math |
;;    |  Accounting |       Bob | spreadsheets |
;;    | Engineering |      Jake |       coding |
;;    | Engineering |      Lisa |       coding |
;;    | Engineering |      Jake |        linux |
;;    | Engineering |      Lisa |        linux |
;;    |          HR |       Sue | spreadsheets |
;;    |          HR |       Sue | organization |

["These three types of joins can be used with other Pandas tools to implement a
wide array of functionality. But in practice, datasets are rarely as clean as
the one we're working with here. In the following section we'll consider some of
the options provided by pd.merge() that enable you to tune how the join
operations work."]

["## Specification of the Merge Key"]

["We've already seen the default behavior of pd.merge(): it looks for one or
more matching column names between the two inputs, and uses this as the
key. However, often the column names will not match so nicely, and pd.merge()
provides a variety of options for handling this."]

["### The on keyword"]

["Most simply, you can explicitly specify the name of the key column using the
on keyword, which takes a column name or a list of column names:"]

^kind/dataset
(tablecloth/inner-join ds1 ds2 :employee)
;; => inner-join [4 3]:
;;    | :employee |      :group | :hire-date |
;;    |-----------|-------------|------------|
;;    |      Lisa | Engineering |       2004 |
;;    |       Bob |  Accounting |       2008 |
;;    |      Jake | Engineering |       2012 |
;;    |       Sue |          HR |       2014 |

["This option works only if both the left and right DataFrames have the
specified column name."]

["### The left_on and right_on keywords"]

["At times you may wish to merge two datasets with different column names; for
example, we may have a dataset in which the employee name is labeled as \"name\"
rather than \"employee\". In this case, we can use the left_on and right_on
keywords to specify the two column names:"]

(def ds3 (tablecloth/dataset {:name ["Bob" "Jake" "Lisa" "Sue"]
                              :salary [70000 80000 120000 90000]}))

ds1
;; => _unnamed [4 2]:
;;    | :employee |      :group |
;;    |-----------|-------------|
;;    |       Bob |  Accounting |
;;    |      Jake | Engineering |
;;    |      Lisa | Engineering |
;;    |       Sue |          HR |

ds3
;; => _unnamed [4 2]:
;;    | :name | :salary |
;;    |-------|---------|
;;    |   Bob |   70000 |
;;    |  Jake |   80000 |
;;    |  Lisa |  120000 |
;;    |   Sue |   90000 |

(require '[tech.v3.dataset.join :as join])
(require '[tech.v3.dataset :as dataset])

^kind/dataset
(join/left-join [:employee :name] ds1 ds3)
;; => left-outer-join [4 4]:
;;    | :employee |      :group | :name | :salary |
;;    |-----------|-------------|-------|---------|
;;    |       Bob |  Accounting |   Bob |   70000 |
;;    |      Jake | Engineering |  Jake |   80000 |
;;    |      Lisa | Engineering |  Lisa |  120000 |
;;    |       Sue |          HR |   Sue |   90000 |


["The result has a redundant column that we can drop if desired–for example, by
using the drop-columns method of tablecloth:"]

^kind/dataset
(-> (join/right-join [:employee :name] ds1 ds3)
    (dataset/drop-columns [:name]))
;; => right-outer-join [4 3]:
;;    | :employee |      :group | :salary |
;;    |-----------|-------------|---------|
;;    |       Bob |  Accounting |   70000 |
;;    |      Jake | Engineering |   80000 |
;;    |      Lisa | Engineering |  120000 |
;;    |       Sue |          HR |   90000 |

["or you can just use inner-join from tech.ml.dataset:"]

^kind/dataset
(join/inner-join [:employee :name] ds1 ds3)
;; => inner-join [4 3]:
;;    | :employee |      :group | :salary |
;;    |-----------|-------------|---------|
;;    |       Bob |  Accounting |   70000 |
;;    |      Jake | Engineering |   80000 |
;;    |      Lisa | Engineering |  120000 |
;;    |       Sue |          HR |   90000 |
