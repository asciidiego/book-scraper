(ns book-scraper
  (:require [hickory.core :refer [parse as-hickory]]
            [hickory.select :as $]
            [clj-yaml.core :as yaml]
            [clojure.java.io :refer [writer]]))

(defn log [msg]
  (println (str "-> " msg)))

(log "Scraping books from CleanCoder website...")

(def result (->> "http://cleancoder.com/books"
                 slurp
                 parse
                 as-hickory
                 ($/select ($/or ($/class "book-title")
                                 ($/class "book-author")))
                 (partition 2 2)
                 (map (fn [book-author-pair]  ; maps book-author -> ("book-title" "author")
                        (->> (map :content book-author-pair)
                             flatten)))
                 (map (fn [[title author]]
                        {:title title
                         :author author}))))
(def output-path "out/books.yaml")
(log (str "Saving results in " output-path "..."))
(with-open [w (writer "out/books.yaml")]
  (.write w (yaml/generate-string result :dumper-options {:flow-style :block})))
