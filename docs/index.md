SciCloj is the Clojure data science community that is developing a series of jupyter style journals to demonstrate how to achieve specific data science tasks using the Clojure programming languages.

[scicloj/notespace project](https://github.com/scicloj/notespace) is used to develop each journal by rendering a Clojure namespace, referred to as a notespace.  Each Clojure top-level form in a namespace is a note in the notespace and can be tagged a specific kind (type of note).

### Contributing examples
The [Python Data Science Handbook](https://jakevdp.github.io/PythonDataScienceHandbook/) is one source of inspiration for the project.  Choose a chapter or sub-chapter in this book and consider how you would do similar tasks using Clojure and the wide range of libraries it has.

Each chapter or sub-chapter has its own namespace, so review the [`src/scicloj` directory](https://github.com/scicloj/scicloj-data-science-handbook/tree/live/src/scicloj) to see which chapters are already started.

Use the [Clojurians Zulip #ml-study stream](https://clojurians.zulipchat.com/#narrow/stream/264992-ml-study) to discuss chapters you wish to work on and get help with Clojure and its libraries.

Please use other sources of data science content for inspiration, ideally referencing them in the notespace as you do so.

See the [getting started section](getting-started.md) to start working with the project.


## Static Journals
Static Journals are [generated by notespace](getting-started.md#generate-a-static-journal) from each Clojure namespace to create a separate chapter for the handbook

* [Helper functions: vega style visualisations with hanami](scicloj/helpers/vega/)
* [Chapter 3: Data Manipulation with Dataset](scicloj/03-data-manipulation/00-data-manipulation)
  * [3.1 Introduction to Dataset](scicloj/03-data-manipulation/01-introducing-dataset)
  * [3.2 Data Indexing and Selection](scicloj/03-data-manipulation/02-data-indexing-and-selection)
  * [3.3 Operating on Data in Dataset](scicloj/03-data-manipulation/03-operations-in-dataset)
  * [3.4 Handling Missing Data](scicloj/03-data-manipulation/04-missing-values)
