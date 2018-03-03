(ns movies-2017-kixi.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [kixi.stats.core :as k]
            [clojure.string :as s])
  (:gen-class))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map (comp keyword
                    #(s/replace % #" " "_")
                    #(s/replace % #"[()]" "")))
            repeat)
       (rest csv-data)))

(defn update-vals [map vals f]
  (reduce #(update-in % [%2] f) map vals))

(defn val-to-int [m key]
  (if (pos? (count (key m)))
    (update m key #(read-string %))
    (assoc m key nil)))

(defn add-watch-year [record]
  (let [year (-> record :Created (subs 0 4) read-string)]
    (assoc record :Watch_Year year)))

(defn load-data []
  (with-open [reader (io/reader "./data/WATCHLIST.csv")]
    (doall
     (->> (csv/read-csv reader)
          csv-data->maps
          (map (fn [record]
                 (-> record
                     (val-to-int :Year)
                     (val-to-int :Your_Rating)
                     (val-to-int :Num_Votes)
                     (val-to-int :Position)
                     (val-to-int :IMDb_Rating)
                     (val-to-int :Runtime_mins))))
          (map add-watch-year)))))

(defn time-report [mins]
  (let [hours (/ mins 60.0)
        days (/ hours 24)]
    {:mins mins :hours hours :days days}))

(defn total-lengths-in-year [data year]
  (transduce
    (comp (filter #(= year (:Watch_Year %)))
          (map :Runtime_mins)
          (filter number?)) + data))

(defn total-lengths-in-year* [data year]
  (->> data
       (filter #(= year (:Watch_Year %)))
       (map :Runtime_mins)
       (reduce +)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
