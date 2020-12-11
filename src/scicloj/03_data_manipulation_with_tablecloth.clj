(ns scicloj.03-data-manipulation-with-tablecloth
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
  (notespace/eval-this-notespace))

;; Chapter 03 - Data Manipulation with tablecloth
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;

["# Chapter 03 - Data Manipulation with tablecloth"]

["In the previous chapter, we dove into detail on (...)

In this chapter, we will focus on the mechanics of using datasets (?Series, DataFrame?), and related structures effectively. We will use examples drawn from real datasets where appropriate, but these examples are not necessarily the focus."]

["## Installing and Using tablecloth"

 "Installation of ..."]

(require '[clojure.java.io :as io]
         '[tablecloth.api :as tablecloth]
         '[scicloj.helpers :as helpers])


["## The tablecloth dataset (Series?) Object

A tablecloth dataset is a (...) It can be created from a map as follows:
"]

(def population-map {:California 38332521,
                     :Texas 26448193,
                     :New_York 19651127,
                     :Florida 19552860,
                     :Illinois 12882135})

(def population (tablecloth/dataset population-map
                                    {:dataset-name "Population"}))

^kind/dataset
population

["By default, (...). From here, typical dictionary-style item access can be performed:"]

(:California population)

["(this ^ is actually a column, not a number)"]

["Unlike a map (dictionary), though, the Series also supports array-style operations such as slicing (FIXME):"]

["(...)"]

(vals population)
(keys population)

