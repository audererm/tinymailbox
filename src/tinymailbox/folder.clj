(ns tinymailbox.folder
  (:import [javax.mail.search SearchTerm AndTerm SubjectTerm BodyTerm]))

(defn search [f query]
  (let [search-term (AndTerm. (SubjectTerm. query) (BodyTerm. query))]
    (.search f search-term)))