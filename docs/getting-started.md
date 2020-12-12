## Getting started
Clone the [scicloj/scicloj-data-science-handbook project](https://github.com/scicloj/scicloj-data-science-handbook)

Install [Clojure CLI tools](https://practicalli.github.io/clojure/clojure-tools/install/) to contribute to developing the project, along with a [Clojure aware editor](https://practicalli.github.io/clojure/clojure-editors/).


## Developing with the project
Open the project in a [Clojure aware editor](https://practicalli.github.io/clojure/clojure-editors/) and start a REPL (either on the command line or from the editor itself).

### Starting notespace
Start the Notespace browser by evaluating the command `(notespace/init-with-browser)` in the editor attached to the REPL.

Start writing the Clojure top-level forms that make up your notebook.

Evaluate `(notespace/eval-this-notespace)` to evaluate all the top-level forms once in the notespace.

Evaluate `(notespace/listen)` to update the notespace each time the file is saved.

If you wish to clear the notebook, evaluate `(notespace/init)`
