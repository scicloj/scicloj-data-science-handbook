(ns scicloj.helpers.vega
  (:require [notespace.kinds :as kind]))

(require '[aerial.hanami.common :as hanami-common]
         '[aerial.hanami.templates :as hanami-templates])

["Similar to pairplots (e.g., [those of Seaborn](https://seaborn.pydata.org/generated/seaborn.pairplot.html)), we sometimes want to have a matrix of scatterplots of all pairs of numerical variables in a dataset.
The following is based on [an example of the Vega-Lite tutorials](https://vega.github.io/vega-lite/examples/interactive_splom.html), generalized using Hanami."]

(def interactive-scatterplot-matrix
  (merge
   hanami-templates/view-base
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
      {:condition {:selection "brush", :field :COLOR-FIELD, :type "nominal"},
       :value     "grey"}}}}))

["Let us try it (automatically enjoying the Hanami theme, etc.)."]

(require '[scicloj.data :as data]
         '[tablecloth.api :as tablecloth])

^kind/vega
(hanami-common/xform
 interactive-scatterplot-matrix
 :DATA (-> data/iris
           (tablecloth/rows :as-maps))
 :ROWS [:sepal-length :sepal-width :petal-length :petal-width],
 :COLUMNS [:sepal-length :sepal-width :petal-length :petal-width]
 :COLOR-FIELD :species)

["Try to interact with it!"]
