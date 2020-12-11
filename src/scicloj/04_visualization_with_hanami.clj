(ns scicloj.04-visualization-with-hanami
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]
            [clojure.java.io :as io]))

;; Notespace
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Listen for changes in the namespace and update notespace automatically
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
  (notespace/eval-this-notespace)
  )

;; Chapter 04 - Visualization with Hanami
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;

["# Visualization with Hanami"]

["This chapter will dive into practical aspects of visualizing data using the Clojure library Hanami and other tools (TODO)"]

["## General Matplotlib Tips"

 "Before we dive into the details of creating visualizations with Hanami, there are a few useful things you should know about using the package."]

["### Importing Hanami"]

(require '[clojure.java.io :as io]
         '[tablecloth.api :as tablecloth]
         '[scicloj.helpers :as helpers]
         '[aerial.hanami.common :as hanami-common]
         '[aerial.hanami.templates :as hanami-templates])



["### Setting Styles"
 "We will use the plt.style directive to choose appropriate aesthetic styles for our figures. Here we will set the classic style, which ensures that the plots we create use the classic Matplotlib style:"]

;;todo

["Throughout this section, we will adjust this style as needed. Note that the stylesheets used here are supported as of Matplotlib version 1.5; if you are using an earlier version of Matplotlib, only the default style is available. For more information on stylesheets, see Customizing Matplotlib: Configurations and Style Sheets."]

["### `show()` or `No show()`? How to Display Your Plots"

 "A visualization you can't see won't be of much use, but just how you view your Matplotlib plots depends on the context. The best use of Matplotlib differs depending on how you are using it; roughly, the three applicable contexts are using Matplotlib in a script, in an IPython terminal, or in an IPython notebook."]

["#### Plotting from a script"

 "If you are using Matplotlib from within a script, the function plt.show() is your friend. plt.show() starts an event loop, looks for all currently active figure objects, and opens one or more interactive windows that display your figure or figures.

So, for example, you may have a file called myplot.py containing the following:"
 ]
;; todo
["You can then run this script from the command-line prompt, which will result in a window opening with your figure displayed:"]
;; todo
["The plt.show() command does a lot under the hood, as it must interact with your system's interactive graphical backend. The details of this operation can vary greatly from system to system and even installation to installation, but matplotlib does its best to hide all these details from you."
 "One thing to be aware of: the plt.show() command should be used only once per Python session, and is most often seen at the very end of the script. Multiple show() commands can lead to unpredictable backend-dependent behavior, and should mostly be avoided."]

["#### Plotting from an IPython shell"
 "It can be very convenient to use Matplotlib interactively within an IPython shell (see IPython: Beyond Normal Python). IPython is built to work well with Matplotlib if you specify Matplotlib mode. To enable this mode, you can use the %matplotlib magic command after starting ipython:"]
;; todo

["At this point, any plt plot command will cause a figure window to open, and further commands can be run to update the plot. Some changes (such as modifying properties of lines that are already drawn) will not draw automatically: to force an update, use plt.draw(). Using plt.show() in Matplotlib mode is not required."]


["#### Plotting from an Notespace notebook"
 "The IPython notebook is a browser-based interactive data analysis tool that can combine narrative, code, graphics, HTML elements, and much more into a single executable document (see IPython: Beyond Normal Python).

Plotting interactively within an IPython notebook can be done with the %matplotlib command, and works in a similar way to the IPython shell. In the IPython notebook, you also have the option of embedding graphics directly in the notebook, with two possible options:

    %matplotlib notebook will lead to interactive plots embedded within the notebook
    %matplotlib inline will lead to static images of your plot embedded in the notebook

After running this command (it needs to be done only once per kernel/session), any cell within the notebook that creates a plot will embed a PNG image of the resulting graphic:

TO-DO need to clean all this:"

 ]

(def sin #(Math/sin %))
(def cos #(Math/cos %))

(defn row [x f l]
  {:x x
   :y (f x)
   :label l})

(defn plot-data [coll]
  (into (map (fn [n] (row n sin "-")) coll)
        (map (fn [n] (row n cos "--")) coll)))

(def my-figure
  (->
 (range 1 10 1/10)
 plot-data
 (#(hanami-common/xform
    hanami-templates/line-chart
    :WIDTH 700 :HEIGHT 500
    :ENCODINGS {:X :x
                :Y :y}
    :DATA %))
 (assoc-in [:encoding :strokeDash]
           {:field :label
            :type "nominal"})))

^kind/vega
my-figure

"### Saving Figures to File

One nice feature of Matplotlib is the ability to save figures in a wide variety of formats. Saving a figure can be done using the savefig() command. For example, to save the previous figure as a PNG file, you can run this:"

(require '[applied-science.darkstar :as darkstar]
         '[clojure.data.json :as json])

(defn save-plot! [m & [filename]]
  (let [svg-render (if (= (:MODE m) "vega")
                 darkstar/vega-spec->svg
                 darkstar/vega-lite-spec->svg)]
   (->>
    m
    json/write-str
    svg-render
    (spit (or filename "plot.svg")))))


(save-plot! my-figure "my_figure.svg")



