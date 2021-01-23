(ns scicloj.data
  (:require [notespace.api :as notespace]
            [notespace.kinds :as kind]
            [clojure.java.io :as io]
            [tablecloth.api :as tc]
            )
  (:import [org.rauschig.jarchivelib ArchiverFactory ArchiveFormat CompressionType])
  )


(defn download-file [url dir]
  (let [my-file (last (.split url "/"))]
    (if-not (.exists  (io/as-file (str dir my-file)))
      (try
        (with-open [ in (io/input-stream url)
                    out (io/output-stream (str dir my-file))]
          (io/copy in out)
          (println  my-file " has been downloaded.")
          )
        (catch Exception e (str "caught exception:" (.getMessage e))))
      (print my-file "is already there"))))


(defn- read-group-news-20 [train-or-test]
  (let [temp-dir (str (System/getProperty "java.io.tmpdir" ) "/")
        data
        (->> (file-seq (io/file (str temp-dir  "20news-bydate-" (name train-or-test))))
             (filter #(.isFile %))
             (map  #(hash-map
                     :text (slurp (.getPath %))
                     :category (.getName (.getParentFile %))))


             )]
    (tc/dataset {:text (map :text data )
                 :category (map :category data)})
    )

  )

(defn news-group-20-ds []
  (let [temp-dir (str  (System/getProperty "java.io.tmpdir" ) "/")
        _ (download-file  "http://qwone.com/~jason/20Newsgroups/20news-bydate.tar.gz"
                          temp-dir)
        archiver (ArchiverFactory/createArchiver ArchiveFormat/TAR CompressionType/GZIP )
        _ (.extract archiver (io/file (str temp-dir "/20news-bydate.tar.gz"))
                    (io/file temp-dir) )

        ]
    {:train (read-group-news-20 :train)
     :test (read-group-news-20 :test) }


    ))


["# Datasets"]

["## The Iris dataset"]

(require '[clojure.java.io :as io]
         '[tech.v3.dataset :as dataset]
         '[tablecloth.api :as tablecloth]
         '[scicloj.helpers :as helpers])








(def iris
  (-> "data/iris.csv"
      io/resource
      (.toString)
      (tablecloth/dataset {:key-fn helpers/->tidy-name})
      (dataset/column-cast :species [:keyword keyword])))

^kind/dataset
iris
