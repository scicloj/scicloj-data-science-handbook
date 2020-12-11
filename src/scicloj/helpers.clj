(ns scicloj.helpers
  (:require [clojure.string :as string]))

(defn ->tidy-name
  "Clean column names"
  [raw-name]
  (-> raw-name
      name
      string/lower-case
      (string/replace #" |/|\.|:|\(|\)|\[|\]|\{|\}" "-")
      (string/replace #"--+" "-")
      (string/replace #"-$" "")
      keyword))
