(ns scicloj.04-visualization.02_data_types_marks_encoding
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]))

;; Notespace
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Listen for changes in the namespace and update notespace
;; automatically
;; Hidden kinds should not show in the notespace page
^kind/hidden
(comment
 ;; Manually start an empty notespace
 (notespace/init-with-browser)
 ;; Renders the notes and listens to file changes
 (notespace/listen)
 ;; Clear an existing notespace browser
 (notespace/init)
 ;; Evaluating a whole notespace
 (notespace/eval-this-notespace))


["# Data Types, Graphical Marks, and Visual Encoding Channels"]
["Aliquam erat volutpat.  Nunc eleifend leo vitae magna.  In id erat non orci commodo lobortis.  Proin neque massa, cursus ut, gravida ut, lobortis eget, lacus.  Sed diam.  Praesent fermentum tempor tellus.  Nullam tempus.  Mauris ac felis vel velit tristique imperdiet.  Donec at pede.  Etiam vel neque nec dui dignissim bibendum.  Vivamus id enim.  Phasellus neque orci, porta a, aliquet quis, semper a, massa.  Phasellus purus.  Pellentesque tristique imperdiet tortor.  Nam euismod tellus id erat.

"]


(require '[aerial.hanami.common :as hanami-common]
         '[aerial.hanami.templates :as hanami-templates]
         '[tech.v3.datatype.functional :as dtype-func]
         '[fastmath.core :as fastmath]
         '[tablecloth.api :as tablecloth]
         '[clojure.data.json :as json])

["## Getting the Data"]
["Getting the Gapminder data from Github"]
(def gapminder
  (json/read-json
   (slurp
    "https://raw.githubusercontent.com/vega/vega-datasets/master/data/gapminder.json")
   {:key-fn keyword}))

["Let's looka at the first rows of the data"]

^kind/dataset (tablecloth/dataset gapminder)


^kind/dataset
(-> gapminder
    tablecloth/dataset
    (tablecloth/select-rows (comp #(= % 2000) :year)))
["## Data types"]
["In statistics and visualization we have a couple of different data types that we will want to behave differently when processed and visualized. Vega/Vega-lite supports four different types"]
["### Nominal"]
["Nominal data is categorical data and consists of categories. Categories reflect which part of the data belongs together, which can be reflected in the visualizations for instance with color or shapes. The `:country` column in the Gapminder data is categorical"]
["### Ordinal"]
["When we talk of ordinal data we want to express that a data has a specific ordering. We can express this in visualizations as rank-orders. `:year` can be treated as ordinal data in our dataset."]
["### Quantitative"]
["With quantitatie data we usually mean continous variables. These are numerical values that differ from datapoint to datapoint. `:fertility` and `:life_expect` are quantitative data."]
["### Temporal"]
["Temporal data expresses time points or intervals. Often we can use the time on the x-axis and the variable that expresses that time in the dataset is temploral. `:year` in the dataset above can be used as a temporal datatype."]



["## Global Development Data"]
["Proin quam nisl, tincidunt et, mattis eget, convallis nec, purus.  "]
["PLOTS"]


["## Data Types"]
["Vestibulum convallis, lorem a tempus semper, dui dui euismod elit, vitae placerat urna tortor vitae lacus.  "]
["PLOTS"]

["### Nominal (N)"]
["Nullam libero mauris, consequat quis, varius et, dictum id, arcu.  "]
["PLOTS"]

["### Ordinal (O)"]
["Nullam libero mauris, consequat quis, varius et, dictum id, arcu.  "]
["PLOTS"]

["### Quantitative (Q)"]
["Nullam libero mauris, consequat quis, varius et, dictum id, arcu.  "]
["PLOTS"]

["### Temporal (T)"]
["Nullam libero mauris, consequat quis, varius et, dictum id, arcu.  "]
["PLOTS"]

["### Summary  "]
["Pellentesque dapibus suscipit ligula.  Donec posuere augue in quam.  Etiam vel tortor sodales tellus ultricies commodo.  Suspendisse potenti.  Aenean in sem ac leo mollis blandit.  Donec neque quam, dignissim in, mollis nec, sagittis eu, wisi.  Phasellus lacus.  Etiam laoreet quam sed arcu.  Phasellus at dui in ligula mollis ultricies.  Integer placerat tristique nisl.  Praesent augue.  Fusce commodo.  Vestibulum convallis, lorem a tempus semper, dui dui euismod elit, vitae placerat urna tortor vitae lacus.  Nullam libero mauris, consequat quis, varius et, dictum id, arcu.  Mauris mollis tincidunt felis.  Aliquam feugiat tellus ut neque.  Nulla facilisis, risus a rhoncus fermentum, tellus tellus lacinia purus, et dictum nunc justo sit amet elit.

"]



["## Encoding Channels"]
["Nullam libero mauris, consequat quis, varius et, dictum id, arcu.  "]
["PLOTS"]





(defn hanami-plot
  "Syntactic sugar for hanami plots, lets you pipe data directly in a thread first macro"
  [data template & substitutions]
  (apply hanami-common/xform
         template
         :DATA
         data
         substitutions))




;; ^kind/vega
;; (->> sin-cos-data
;;      (hanami-plot hanami-templates/line-chart)
;;      (#(assoc-in %
;;         [:encoding :strokeDash]
;;         {:field :label :type "nominal"})))
