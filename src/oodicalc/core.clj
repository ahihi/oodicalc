(ns oodicalc.core
  (:import
    (javax.swing Box JFrame JLabel JTextArea JButton JScrollPane ScrollPaneConstants)
    (java.awt.event ActionListener)
    (java.awt Dimension GridLayout)
    (java.util.regex Pattern))
  (:require
    [clojure.contrib.string :as string])
  (:gen-class))

(defrecord Course [id name credits grade date grader])

(def tab-pattern
  (Pattern/compile "\t"))

(defn try-parse-int [txt]
  (try
    (Integer/parseInt txt)
    (catch NumberFormatException e
      nil)))

(defn parse-course-line [line]
  (let [[id name credits grade date grader]
          (map string/trim (string/split tab-pattern line))]
    (if (every? empty? [id name])
      :ignore
      (if (nil? grader)
        :error
        (try
          (Course. id
                   name
                   (Double/parseDouble credits)
                   (try-parse-int grade)
                   date
                   grader)
          (catch NumberFormatException e
            :error))))))

(defn parse-courses [lines]
  (loop [courses []
         lines-left lines]
    (if (empty? lines-left)
      [:ok courses]
      (let [line (first lines-left)
            course (parse-course-line line)]
        (case course
          :error
            [:error line]
          :ignore
            (recur courses (rest lines-left))
          (recur (conj courses course)
                 (rest lines-left)))))))

(defn has-grade? [course]
  (not (nil? (get course :grade))))

(defn average-with [f courses]
  (let [divide (fn [a b]
                 (if (zero? b)
                   nil
                   (/ a b)))]
    (apply divide (reduce f [0 0] courses))))

(defn naive-grade [[gr-sum cnt] course]
  (let [grade (get course :grade)]
    (if (nil? grade)
      [gr-sum cnt]
      [(+ gr-sum grade) (inc cnt)])))

(defn weighted-grade [[crgr-sum cr-sum] course]
  (let [grade (get course :grade)
        credits (get course :credits)]
    (if (nil? grade)
      [crgr-sum cr-sum]
      [(+ crgr-sum (* credits grade)) (+ cr-sum credits)])))

(defn display-average [x]
  (if (nil? x)
    "n/a"
    (format "%.2f" (double x))))

(defn str-stats [oodi-text]
  (let [[status result] (parse-courses (string/split-lines oodi-text))]
    (case status
      :ok
        (let [result-lines
               [(str (count result) " courses ("
                     (count (filter has-grade? result)) " graded)")
                (str (reduce #(+ %1 (get %2 :credits)) 0 result) " credits")
                (str "Average grade (naive): "
                     (display-average (average-with naive-grade result)))
                (str "Average grade (weighted): "
                     (display-average (average-with weighted-grade result)))]]
          (string/join \newline result-lines))
      :error
        (let [error-text (if (empty? (string/trim result))
                           "empty line"
                           result)]
          (str "Parse error: " error-text)))))

(defn set-font-size [component size]
  (.setFont component (.deriveFont (.getFont component) (float size))))

(defn -main [& args]
  (let [frame (JFrame. "OodiCalc")
        oodi-label-box (Box/createHorizontalBox)
        oodi-label (JLabel. "Oodi data:")
        oodi-textarea (JTextArea. 1 1)
        oodi-scrollpane
          (JScrollPane. oodi-textarea
                        ScrollPaneConstants/VERTICAL_SCROLLBAR_AS_NEEDED
                        ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER)
        stats-label-box (Box/createHorizontalBox)
        compute-button (JButton. "Compute")
        stats-label (JLabel. "Statistics:")
        stats-textarea (JTextArea. 1 1)
        stats-scrollpane
          (JScrollPane. stats-textarea
                        ScrollPaneConstants/VERTICAL_SCROLLBAR_AS_NEEDED
                        ScrollPaneConstants/HORIZONTAL_SCROLLBAR_NEVER)
        box (Box/createVerticalBox)]
    (doto oodi-label-box
      (.add oodi-label)
      (.add (Box/createHorizontalGlue)))
    (doto oodi-textarea
      (set-font-size 12)
      (.setTabSize 4)
      (.setLineWrap true))
    (doto stats-label-box
      (.add stats-label)
      (.add (Box/createHorizontalGlue))
      (.add compute-button))
    (doto stats-textarea
      (set-font-size 12)
      (.setLineWrap true)
      (.setEditable false))
    (doto box
      (.add oodi-label-box)
      (.add oodi-scrollpane)
      (.add stats-label-box)
      (.add stats-scrollpane))
    (.addActionListener compute-button
                        (reify ActionListener
                          (actionPerformed [this e]
                            (.setText stats-textarea
                                      (str-stats (.getText oodi-textarea))))))
    (doto frame
      (.setDefaultCloseOperation (JFrame/EXIT_ON_CLOSE))
      (.add box)
      (.setMinimumSize (Dimension. 300 300))
      (.setPreferredSize (Dimension. 640 480))
      (.pack)
      (.setVisible true))))
