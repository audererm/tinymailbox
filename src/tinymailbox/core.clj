(ns tinymailbox.core
  (:gen-class)
  (:require [clojure-mail.core :as mail]
            [clojure-mail.message :as msg]
            [tinymailbox.folder :as folder]))

(def folder (str (System/getenv "HOME") "/.tinymailbox/"))

(declare mail-repl login)

(defn show-box
  [box user store]
  (doall (map #(println (:subject (msg/read-message %))) (mail/all-messages store box)))
  (mail-repl user store))

(defn show-inbox
  [user store]
  (show-box :inbox user store))

(defn show-outbox
  [user store]
  (show-box :sent user store))

(defn show-spam
  [user store]
  (show-box :spam user store))

(defn show-all
  [user store]
  (show-box :all user store))

(defn search-inbox
  "Search your inbox for a specific term
   Returns a vector of IMAPMessage objects"
  [store term]
  (let [inbox (mail/open-folder store :sent :readonly)]
    (into []
      (folder/search inbox term))))

(defn search
  [term user store]
  (let [results (search-inbox store term)]
    (if (= (count results) 0)
      (println "No matching messages found.")
      (doall (map #(println (:subject (msg/read-message %))) results))))
  (mail-repl user store))

(defn read-all 
  [user store]
  (mail/mark-all-read :inbox)
  (mail-repl user store))

(defn unknown
  [command user store]
  (println (str "Unknown command \"" command "\""))
  (mail-repl user store))

(defn switch-account
  [username]
  (login (->> (clojure.string/split (slurp (str folder username)) #"\n")
        	(map #(clojure.string/split % #"\: "))
   		    (map (fn [[k v]] [(keyword k) v]))
        	(into {}))))

(defn mail-repl
  [user store]
  (let [] ; let may not be needed here, remove later if not used
    (print (str user "> "))
    (flush)
    (let [raw-input (clojure.string/lower-case (read-line))
          args (clojure.string/split raw-input #" ")
          command (first args)]
      (case command
        ("exit" "quit" "bye") (println "Shutting down mail repl.")
        ("inbox" "received") (show-inbox user store)
        ("outbox" "sent") (show-outbox user store)
        ("spam") (show-spam user store)
        ("all") (show-all user store)
        ("readall") (read-all user store)
        ("search" "find") (search (second args) user store)
        ("switch" "su") (switch-account (second args))
        (unknown command user store)))))

(defn login
  [userinfo]
  (let [user (:username userinfo)
        store (mail/gen-store user (:password userinfo))]
    (mail-repl user store)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (login (->> (clojure.string/split (slurp (str folder "tmbrc")) #"\n")
        	(map #(clojure.string/split % #"\: "))
   		    (map (fn [[k v]] [(keyword k) v]))
        	(into {}))))