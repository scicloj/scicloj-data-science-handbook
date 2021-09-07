(ns scicloj.04-visualization.00-table-of-content
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

["# Visualization using Vega/Vega-lite and Hanami"]

["This chapter will dive into practical aspects of visualizing data using the Clojure library Hanami and other tools. Using exaples we will familiarize with the Vega-Lite declarative visualisation API, with the help of Hanami. Common use visualization use cases will be demonstrated."]

["## Table of Contents

1. Introduction
2. Data Types, Graphical Marks, and Visual Encoding Channels
3. Data Transformation
4. Scales, Axes, and Legends
5. Multi-View Composition
6. Interaction
7. Visualization of maps"]

