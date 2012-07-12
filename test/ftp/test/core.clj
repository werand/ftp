(ns ftp.test.core
  (:use [ftp.core])
  (:use [clojure.test]))

(deftest replace-me ;; FIXME: write
  (is false "No tests have been written."))

#_(def ^:dynamic *connection* {
                 :url "192.168.178.21",
                 :user "xxx",
                 :password "xxx",
                 :transfer-mode :binary})
;; Transfer-mode may be one of :binary or :ascii if none is specified it will be :binary 

; false
#_(with-ftp *connection*
  (exists-remotely? "some/file"))

; true
#_(with-ftp *connection*
  (exists-remotely? "andreas/test.txt"))

#_(with-ftp *connection*
  (slurp "andreas/test.txt"))

;; Binding of ftp-client to the ftp-client, so any method can be called on the client-api
#_(with-ftp-client ftp-client *connection*
  (.logout ftp-client)
  (prn (exists-remotely? "some/file")))

#_(with-ftp *connection*
  (spit "andreas/test.txt" "This is a test"))
