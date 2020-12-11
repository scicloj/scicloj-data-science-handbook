# SciCloj Clojure Data Science Handbook
A reference style resource to help build understanding in how to use Clojure for data science projects.

Inspired by the [Python Data Science Handbook](https://jakevdp.github.io/PythonDataScienceHandbook/), the project creates a series of journal notebooks based on the chapters in this book.

Notebooks are created with the [scicloj/notespace library](https://github.com/scicloj/notespace), which generates a notebook for a specific Clojure namespace.  Notebooks can be dynamically generated during development.  Static versions of the notebooks will be saved in the `docs/` directory and served via GitHub pages.


## Getting started

Clone the project from https://github.com/scicloj/data-science-handbook

## Usage
Open the project in a [Clojure aware editor](https://practicalli.github.io/clojure/clojure-editors/) and start a REPL (either on the command line or from the editor itself).

Start the Notespace browser by evaluating the command `(notespace/init-with-browser)` in the editor attached to the REPL.

Start writing the Clojure top-level forms that make up your notebook.

Evaluate `(notespace/eval-this-notespace)` to evaluate all the top-level forms once in the notespace.

Evaluate `(notespace/listen)` to update the notespace each time the file is saved.

If you wish to clear the notebook, evaluate `(notespace/init)`


## Other ways to use the project
Run the project directly:

    $ clojure -M -m scicloj.data-science-handbook

Run the project's tests (they'll fail until you edit them):

    $ clojure -M:test:runner

Build an uberjar:

    $ clojure -M:uberjar

Run that uberjar:

    $ java -jar data-science-handbook.jar


## License
Copyright © 2020 SciCloj
Distributed under the Creative Commons Attribution Share-Alike 4.0 International
