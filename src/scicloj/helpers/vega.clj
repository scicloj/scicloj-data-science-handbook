(ns scicloj.helpers.vega
  (:require [notespace.kinds :as kinds]))

(require '[aerial.hanami.common :as hc]
         '[aerial.hanami.templates :as ht]
         '[aerial.hanami.core :as hmi])

["Similar to pairplots (e.g., [those of Seaborn](https://seaborn.pydata.org/generated/seaborn.pairplot.html)), we sometimes want to have a matrix of scatterplots of all pairs of numerical variables in a dataset.
The following is based on [an example of the Vega-Lite tutorials](https://vega.github.io/vega-lite/examples/interactive_splom.html), generalized using Hanami."]

(def interactive-scatterplot-matrix
  (merge
   ht/view-base
   {:repeat
    {:row    :ROWS
     :column :COLUMNS},
    :spec
    {:mark "point",
     :selection
     {:brush
      {:type    "interval",
       :resolve "union",
       :on      "[mousedown[event.shiftKey], window:mouseup] > window:mousemove!",
       :translate
       "[mousedown[event.shiftKey], window:mouseup] > window:mousemove!",
       :zoom    "wheel![event.shiftKey]"},
      :grid
      {:type    "interval",
       :resolve "global",
       :bind    "scales",
       :translate
       "[mousedown[!event.shiftKey], window:mouseup] > window:mousemove!",
       :zoom    "wheel![!event.shiftKey]"}},
     :encoding
     {:x {:field {:repeat "column"}, :type "quantitative"},
      :y {:field {:repeat "row"}, :type "quantitative", :axis {:minExtent 30}},
      :color
      {:condition {:selection "brush", :field "Origin", :type "nominal"},
       :value     "grey"}}}}))

["Let us try it (automatically enjoying the Hanami theme, etc.)."]

^kinds/vega
(hc/xform
 interactive-scatterplot-matrix
 :UDATA "data/cars.json" ; available as one of the standard Vega example datasets
 :ROWS ["Horsepower" "Acceleration" "Miles_per_Gallon"],
 :COLUMNS ["Miles_per_Gallon" "Acceleration" "Horsepower"])

["Try to interact with it!"]
