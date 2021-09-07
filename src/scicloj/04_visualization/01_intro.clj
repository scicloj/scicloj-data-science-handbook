(ns scicloj.04-visualization.01-intro
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]
            [tablecloth.api :as tc]))

;; Notespace
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Listen for changes in the namespace and update notespace
;; automatically
;; Hidden kinds should not show in the notespace page
^kind/hidden
(comment
 ;; Manually start an empty notespace
 (notespace/init-with-browser)
 ;; the notes and listens to file changes
 (notespace/listen)
 ;; Clear an existing notespace browser
 (notespace/init)
 ;; Evaluating a whole notespace
 (notespace/eval-this-notespace)
 (notespace/render-static-html)
 (gorilla-notes.core/toggle-option! :auto-scroll?)
 nil)



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;

["# Introduction to Vega/Vega-lite and Hanami"]

["  Subjects to briefly mention in summary: 
- The Grammar of Graphics by leland wilkinson 
- Tableau, Vega, ggplot2 
- Declaratrive 
- Other clojure visualization options 
- Templating with hanami"]
["## Starting clojure"]
["Besides of having Clojure installed on nix machines you will need a .clojure/deps.edn file with the alpha version of `add-libs` library. We recommend using the one John Practicalli Stevenson maintains. Install it from here:xxx"]
["When you have that installed you need to start clojure with the `aplpha/hot-load` alias. either by starting it from terminal with `clojure -M:alpha/hot-load:repl/rebel` or if you launch it from cider on emacs you need to do the jack in with the universal argument so that you can "]
["## Requires and dependancies"]

["Tablecloth is the main workhorse that provides an api that lets us manputlate datasets, that are much like dataframes in Python Pandas library or the dplyr library in R. Futhermore, we are loading hanami that helps us writing vega and vega-lite specifications from Clojure. Lets first begin by adding the dependancies ie. installing the libraries."]

["Next, we will require them which makes them available for us in our current program"]

;; aerial.hanami/aerial.hanami {:mvn/version "0.12.4"}
;; generateme/fastmath         {:mvn/version "2.0.5"}
;; techascent/tech.viz         {:mvn/version "0.4.3"}
;; techascent/tech.ml          {:mvn/version
;; "5.00-beta-13-SNAPSHOT"}
;; techascent/tech.ml.dataset  {:mvn/version
;; "5.00-beta-23"}
;; applied-science/darkstar {:git/url
;; "https://github.com/applied-science/darkstar"
;;                           :sha
;;                           "541a3ff36065c59e92fe6aa61e41a4385ba6f893"}
;; batik-rasterize/batik-rasterize {:mvn/version "0.1.2"}

(require '[aerial.hanami.common :as hanami-common]
          '[aerial.hanami.templates :as hanami-templates]
          '[tablecloth.api :as tablecloth]
          '[clojure.data.json :refer [read-json]]
          '[clojure.string :as strings]
          '[clojure.set :as set])


["## Getting the Data"]
^kind/hidden
["DO NOT NEED THIS... Lets use this function to clean column names and convert them so they look more idiomatically Clojure. We will later move this to a helper library."]

^kind/hidden
(defn ->tidy-name
  "Clean column names"
  [raw-name]
  (-> raw-name
      name
      strings/lower-case
      (strings/replace #" |/|\.|:|\(|\)|\[|\]|\{|\}" "-")
      (strings/replace #"--+" "-")
      (strings/replace #"-$" "")
      keyword))

["To start off, we will be loading and defining a couple of datasets we are going to use."]
(def cars-data
  (read-json
   (slurp
    "https://raw.githubusercontent.com/vega/vega-datasets/master/data/cars.json")))



["Let's looka at the first rows of the data"]

^kind/dataset (tablecloth/dataset cars-data {:key-fn ->tidy-name})


["## Weather Data"]
["Lets define some data manually in the :edn format"]
(def life-satisfaction
  [{:country "Sweden" :year 2015 :satisfaction 7.29}
   {:country "Sweden" :year 2016 :satisfaction 7.37}
   {:country "Sweden" :year 2017 :satisfaction 7.29}
   {:country "Poland" :year 2015 :satisfaction 6.01}
   {:country "Poland" :year 2016 :satisfaction 6.16}
   {:country "Poland" :year 2017 :satisfaction 6.20}
   {:country "Portugal" :year 2015 :satisfaction 5.08}
   {:country "Portugal" :year 2016 :satisfaction 5.45}
   {:country "Portugal" :year 2017 :satisfaction 5.71}])

^kind/dataset (tablecloth/dataset life-satisfaction)


["But before we start to play with the life satisfaction data lets first use someting even simpler that we will define next."]

(def some-data
  [{:y "Ada" :x 2.68}  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   {:y "Ada" :x 0.87}  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   {:y "Ada" :x 5.31}  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   {:y "Joan" :x 3.94} ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   {:y "Joan" :x 4.13} ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   {:y "Joan" :x 3.58} ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   {:y "Margaret" :x 3.62} ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   {:y "Margaret" :x 3.98} ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   {:y "Margaret" :x 2.56}])


["## Marks and Encoding "]
["Scatter plot, bar chart, line chart are just names for visualizations that use the dimensonality of the data and different graphical elements in different ways. They all contain same elements such as the data, a geometric objects (or grafical marks in Vega-lite) and some visual encoding channels. Now, lets look at how to work with marks in Vega-lite. This is how we declare a graphical mark"]

^kind/vega {:mark "circle"}

["We do not see anything yet, because we have not provided any data."]

^kind/vega {:data some-data :mark "circle"}

["Now we can see the mark, but does not tell us much yet. So lets add some encodings. Next we will define that we have an y-axis that will represent the `:y` column in our data and that data is `nominal` ie. categorical "]


^kind/vega
{:data     {:values some-data}
 :encoding {:y {:field :y :type "nominal"}}
 :mark     "circle"}

["It still is rather minial in the information it conveys, so lets also ad encode the `:x` column to be displayed on the x-axis. This time our data is continous, so we need to define the type as `quantiative`, which is the term Vega/Vega-lite uses"]

^kind/vega
{:data     {:values some-data}
 :encoding {:x {:field :x :type "quantitative"}
            :y {:field :y :type "nominal"}}
 :mark     "circle"}

["We are not doing very complicated things but the nested declaration is starting go be pretty involved already. Now lets look at how Hanami can help"]

["## Hanami basics"]
["When building Vega and Vega-lite plots we will be using the Hanami library by Jon Anthony. We will go through how it works in more detail later, but for now it is enough to understand that it is a conveniant templating system that we use to create vega specifications. It offers us sensible defauls, so we do not need to type every detail every time and our code becomes more readable. Besides of defaults it also provides with an ergonomic tool to easily change values in the nested data structure that Vega/Vega-lite uses, also this helps the ergonomics and readability of our code. "]
["For our conveniance we will also define a little helper functions here. The first one just centers the plot nicely in the notebook, the second makes our usage of the hanami function a little bit less verbose."]

(defn center-plot
  "inline css for flexbox that centers the plot"
  [plot]
  [:div
   {:style {:aligh-items     :center
            :display         :flex
            :justify-content :center}} [:div [:p/vega plot]]])

(defn hanami-plot
  "convinience function for hanami plots"
  [data template & substitutions]
  (-> hanami-common/xform
      (apply template :DATA data substitutions)))

["### The previous map with hanami"]

["Hanami is less verbose. Lets see what what the specification that hanami produces looks like."]

;;; We are pretty much at the end here. The last thing to
;;; cover concerns what
;;; some have voiced a desire for - changing the default
;;; style of using
;;; :UPPERCASE keywords for substitution keys. Nothing in
;;; any of the
;;; transformation process requires this and you can make
;;; them be any style
;;; or form you like. The following is a little exercise in
;;; how you can
;;; automate that change
;;; We already saw that hanami-common/_defaults is the default submap.
;;; As you can see,
;;; there are a lot of such default keys.j
(deref hanami-common/_defaults)

;; (hanami-common/update-defaults
;;  (->> hanami-common/_defaults
;;       deref
;;       (map (juxt
;;             (comp keyword #(str ">" %) strings/lower-case name key)
;;             val))
;;       (into {}))
;;  (hanami-common/xform (deref hanami-common/_defaults)
;;                       (->> hanami-common/_defaults
;;                            deref
;;                            (mapv (fn [[k v]]
;;                                    (vector (-> k
;;                                                name
;;                                                strings/lower-case
;;                                                (->> (str ">"))
;;                                                keyword)
;;                                            k)))
;;                            (into {})
;;                            set/map-invert)))

;;; We will need clojure.set and clojure.string for the
;;; exercise. Like any
;;; form, requires can be evaluated. Just put the cursor on
;;; the right paren
;;; like any other example, and Ctrl-X Ctrl-E these
(require '[clojure.set :as set])
;;(require '[clojure.string :as str])
;;; We will turn our uppercase keys into lower case
;;; strings.
;;; mydefs is a map
;;; from the uppercase keywords to their lower case
;;; strings.
;;; If you eval this
;;; you will see the first (random) 10 cases. Comment the
;;; take out and
;;; uncomment each xform in turn to see how the templates
;;; can be transformed
;;; to use these lower case substitution keys.
;;; This is just an example, more likely version would use
;;; namespace qualified
;;; lowercase keywords.
(let [mydefs (->> hanami-common/_defaults
                  deref
                  (mapv (fn [[k v]]
                          (vector (-> k
                                      name
                                      strings/lower-case
                                      (->> (str "blabla/"))
                                      keyword)
                                  k)))
                  (into {})
                  set/map-invert)]
  ;;(take 10 mydefs)
  #_(hanami-common/xform hanami-templates/view-base mydefs)
  #_(hanami-common/xform hanami-templates/point-chart mydefs)
  (hanami-common/xform (drop 69 (deref hanami-common/_defaults))
                       mydefs))
;; trying to
(-> some-data
    (hanami-plot hanami-templates/point-chart :YTYPE :nominal))

["As we can see above the output data structure incloudes all kinds of sensible defaults we did not have to declare. Plus we do all the declarations in the root level of the definition althouhg we are actually manipulating values that are in nested structures. It is easier to read and write, although you do need to have a mental model of how the ouptput, the plot specification as edn, looks like to fluently work with it. The principles how these templates and snippets or fragments work are pretty clever, we will go through them a bit later."]
["Let us now look how it renders. Note that the last centering function is just to add the centering html and css. Note also that we are decorating this with the metadata `^kind/hiccup` instead of `^kind/vega` as we are asking notespace to render html instead of a Vega specification."]


^kind/hiccup
(-> some-data
    (hanami-plot hanami-templates/point-chart :YTYPE :nominal)
    center-plot)
["Since the default has height and widths it looks slightly differen than it did in the above example. If we want to erase the with and the height defaults, we can just override them with an empty hash-map."]
^kind/vega
(-> some-data
    (hanami-plot hanami-templates/point-chart
                 :HEIGHT {}
                 :WIDTH  {}
                 :YTYPE  :nominal))

["Now it looks the same, but to be honest usually we will want to define the height and width, so we like them there."]

["## Aggregating and transforming data"]

["Let us first test the same plot with the life satisfaction data. Since the columns are not called `x` and `y` we now need to declare what columns to use."]
^kind/vega
(-> life-satisfaction
    (hanami-plot hanami-templates/point-chart
                 :X      :satisfaction
                 :Y      :country
                 :HEIGHT {}
                 :WIDTH  {}
                 :YTYPE  :nominal))

["Now lets try aggregating the data, which can be done with the `:XAGG` snippet."]

^kind/vega
(-> life-satisfaction
    (hanami-plot hanami-templates/point-chart
                 :X       :satisfaction
                 :Y       :country
                 :HEIGHT  {}
                 :WIDTH   {}
                 :YTYPE   :nominal
                 :XAGG    :mean
                 :tooltip {}));; note, aggregation does not work with the tooltip
;; snippet...


["## Changing mark type"]
["It is trivial to change the mark used to represent the datapoints. Just declare another mark type"]
^kind/vega
(->
  life-satisfaction
  (hanami-plot hanami-templates/bar-chart ;; need to
               ;; change
               ;; template, no
               ;; simple way of
               ;; just changing
               ;; the mark.
               :X       :satisfaction
               :Y       :country
               :HEIGHT  {}
               :WIDTH   {}
               :YTYPE   :nominal
               :XAGG    :mean
               :tooltip {}))


;;hanami-common/_defaults
["To change the orientation we simply need to flip the x and the y"]
^kind/vega
(-> life-satisfaction
    (hanami-plot hanami-templates/bar-chart
                 :X       :country
                 :Y       :satisfaction
                 :HEIGHT  {}
                 :WIDTH   {}
                 :XTYPE   :nominal
                 :YAGG    :mean
                 :tooltip {}))

;; experimenting with syntax
;; (-> life-satisfaction
;;     (hanami-plot hanami-templates/bar-chart
;;                  :>x             :country
;;                  :>y              :satisfaction
;;-height {}
;;                  :rplc-width     {}
;;                  :replace-xtype  :nominal
;;                  ;;                 :YAGG    :mean
;;                  :tooltip        {}))

["## Customizing a Visualization"]

["Both Vega-Lite and Hanami has defaults but we can override them when we want."]

^kind/vega
(-> life-satisfaction
    (hanami-plot
     (assoc-in hanami-templates/point-chart [:mark :type] :point) ;; awkward
                                                                  ;; way
                                                                  ;; of
                                                                  ;; changing
                                                                  ;; mark
     :X       :satisfaction
     :Y       :country
     :HEIGHT  {}
     :YTYPE   :nominal
     :XSCALE  {:type :log} ;; not intuitive to
                           ;; know when to add
                           ;; the type kv and
                           ;; when not
     :MCOLOR  :firebrick
     :XTITLE  "Log-Scaled Satisfaction"
     :YTITLE  "Country"
     :tooltip {}))

["## More Complicated Views"]
["Let's now grab the cars data and first just plot a line chart."]
^kind/hiccup
(-> cars-data
    #_(->> (take 2))
    (hanami-plot hanami-templates/point-chart
                 :X     :Year
                 :Y     :Miles_per_Gallon
                 :XTYPE :temporal
                 :YAGG  :mean)
    center-plot
    )
["Hmm, aggregation of Y values stopped working, needs to be be debugged. Just a quick test that it works with the vega-lite example... from the docs. FIXME"]

^kind/hiccup
(->
 {:$schema "https://vega.github.io/schema/vega-lite/v5.json"
  :data {:url "data/cars.json"}
  :mark :point
  :encoding {
             :x {:field :Cylinders},
             :y {:field :Acceleration :aggregate :mean}
             }
  }
    center-plot
 )

["Next, lets add some points to the line"]

^kind/hiccup
(-> cars-data
    (hanami-plot (update hanami-templates/line-chart
                         :mark
                         #(assoc % :point true)) ;; fix
                                                 ;; template
                 :X     :Year
                 :Y     :Miles_per_Gallon
                 :XTYPE :temporal
                 :YAGG  :mean)
    center-plot)

["Now, sometimes we need to display several plots at the same time. In the following we put them next to each other."]
^kind/hiccup
(-> cars-data
    (hanami-plot hanami-templates/hconcat-chart
                 :HCONCAT
                 [(hanami-common/xform hanami-templates/line-chart
                                       :X     :Year
                                       :Y     :Miles_per_Gallon
                                       :XTYPE :temporal
                                       :YAGG  :mean)
                  (hanami-common/xform hanami-templates/line-chart
                                       :X      :Year
                                       :Y      :Horsepower
                                       :XTYPE  :temporal
                                       :YAGG   :mean
                                       :MCOLOR :red)])
    center-plot)


["## Interactivity"]
["The most simple interactivity is just a tooltip that helps the viewer. Many of the templates have that built in. But you modify it."]

^kind/hiccup
(-> cars-data
    (hanami-plot (-> hanami-templates/point-chart
                     (assoc :color :COLOR)
                     (assoc :tooltip :TOOLTIP))
                 :X       :Horsepower
                 :Y       :Miles_per_Gallon
                 :YAGG    :mean
                 :MTYPE   :point
                 :COLOR   {:field :Origin :type :nominal}
                 :TOOLTIP [{:field :Origin} {:field :Name}])
    center-plot)

["Finally, lets build some interactions between two plots."]
^kind/hiccup
(-> cars-data
    (hanami-plot
     hanami-templates/vconcat-chart
     :VCONCAT
     [(hanami-common/xform
       hanami-templates/bar-chart
       :X         :Year
       :XTYPE     :temporal
       :XTITLE    nil
       :XUNIT     :year
       :YTITLE    nil
       :YAGG      :count
       :HEIGHT    60
       :SELECTION {:brush {:type :interval :encodings [:x]}}
       :OPACITY   {:condition {:selection :brush :value 1} :value 0.2}
       :TITLE     nil)
      (hanami-common/xform hanami-templates/point-chart
                           :X       :Miles_per_Gallon
                           :Y       :Horsepower
                           :OPACITY {:condition {:selection :brush
                                                 :value     0.5}
                                     :value     0.2}
                           :HEIGHT  {})])
    center-plot)
["This starts to be rather involved already, but so is the complexity of the plot we are building. The spec it produces is below."]

(hanami-common/xform
 hanami-templates/vconcat-chart
 :VCONCAT
 [(hanami-common/xform
   hanami-templates/bar-chart
   :X         :Year
   :XTYPE     :temporal
   :XTITLE    nil
   :XUNIT     :year
   :YTITLE    nil
   :YAGG      :count
   :HEIGHT    60
   :SELECTION {:brush {:type :interval :encodings [:x]}}
   :OPACITY   {:condition {:selection :brush :value 1} :value 0.2}
   :TITLE     nil)
  (hanami-common/xform hanami-templates/point-chart
                       :X       :Miles_per_Gallon
                       :Y       :Horsepower
                       :OPACITY {:condition {:selection :brush
                                             :value     0.5}
                                 :value     0.2}
                       :HEIGHT  {})])
["
:>encoding {:bla 123}
:encoding> {:bla 123}
:|encoding= {:bla 123}
:|encoding {:bla 123}
:ENCODING {:bla 123}
:=encoding {:bla 123}
:)endoding
:(endoding
:D-encoding
:D_encoding
"]
