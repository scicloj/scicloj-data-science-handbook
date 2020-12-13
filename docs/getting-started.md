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
`(notespace/render-static-html)` will generate a journal html page from your notespace.  It is highly recommended that you initialize the notespace first, to clear any unwanted notes from the REPL state.

```clojure
(notespace/init)

(notespace/render-static-html "docs/chapter-name/sub-section.html")
```

Edit the `docs/index.md` file and add a link to the generated html page under the [**Static Journals**](https://github.com/scicloj/scicloj-data-science-handbook/blob/live/docs/index.md#static-journals) section.

> Please do not include significant parts of the text from the Python data science book as it has a "No Derivatives" license.

Use the `^kind/hidden` tag on notes you do not wish to include in the generated journal.


#### Helper code
Add the following code to the end of the namespace to help generate a static journal, assuming you do not have key bindings / short-cuts available

Update the chapter and section names for the specific namespace.

```clojure
^kind/hidden
(comment

  (require '[notespace.api :as notespace])

  ;; Clean the notespace in the REPL state
  (notespace/init)

  ;; Generate a static journal as a html page
  (notespace/render-static-html "docs/chapter-name/sub-section.html")
  )
```
