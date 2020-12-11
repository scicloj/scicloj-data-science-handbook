(ns scicloj.data-science-handbook
  (:gen-class)
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]))


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
  )

;; Notespace examples
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Full details at:
;; https://scicloj.github.io/notespace/doc/notespace/v3-experiment1-test/index.html

["Template for each chapter notespace, awesome"]
