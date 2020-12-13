## Getting started
Clone the [scicloj/scicloj-data-science-handbook project](https://github.com/scicloj/scicloj-data-science-handbook)

Install [Clojure CLI tools](https://practicalli.github.io/clojure/clojure-tools/install/) to contribute to developing the project, along with a [Clojure aware editor](https://practicalli.github.io/clojure/clojure-editors/).


## Developing with the project
Open the project in a [Clojure aware editor](https://practicalli.github.io/clojure/clojure-editors/) and start a REPL (either on the command line or from the editor itself).

### Starting notespace
Start the Notespace browser by evaluating the command `(notespace/init-with-browser)` in the editor attached to the REPL.

Start writing the Clojure top-level forms that make up your notespace (journal).

Evaluate `(notespace/eval-this-notespace)` to render all top-level forms in the notespace.

Evaluate `(notespace/listen)` to update the top-level forms in the notespace each time the file is saved.

Evaluate `(notespace/init)` to clear the notespace.


## Generate a static journal
`(notespace/render-static-html)` will generate a journal html page from your notespace.  It is highly recommended that you initialize the notespace first, to clear any unwanted notes from the REP state.

```clojure
(notespace/init)

(notespace/render-static-html)
```

Put the journal html file into the `/docs` directory and add a link to the html page in the `docs/index.md` file, under the **Static Journals** section


> Please do not include significant parts of the text from the Python data science book as it has a "No Derivatives" license.

Use the `^kind/hidden` tag on notes you do not wish to include in the generated journal.
