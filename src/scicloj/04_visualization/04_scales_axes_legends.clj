(ns scicloj.04-visualization.04_scales_axes_legends
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

;; Chapter 04 - Visualization with Hanami
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;

["# Visualization with Hanami"]

^kind/hidden
["This chapter will dive into practical aspects of visualizing data using the Clojure library Hanami and other tools (TODO)"]




(require '[aerial.hanami.common :as hanami-common]
         '[aerial.hanami.templates :as hanami-templates]
         '[tech.v3.datatype.functional :as dtype-func]
         '[fastmath.core :as fastmath]
         '[tablecloth.api :as tablecloth])


["### For our convenience"
 "Lets start by defining a conveniance function that makes plot expression syntax slightly less verbose. The function is built so that it will receive the data we want to visualize as a first argument and thus enabling it to be used as is in a thread first, `->`, macro at the end of our data transformations"]

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
