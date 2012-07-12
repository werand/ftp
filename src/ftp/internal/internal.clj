;; Copyright (c) Andreas Werner. All rights reserved. The use and
;; distribution terms for this software are covered by the Eclipse Public
;; License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
;; be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other,
;; from this software.
;;
;; internal.clj
;;
;; A clojure interface to FTP via the apache commons-net ftp client
;;
;; werner_andreas (@web.de)
;; Created 18 March 2012
(ns
  ftp.internal.internal
  (:import 
    (java.io IOException FileOutputStream FileInputStream File ByteArrayOutputStream ByteArrayInputStream ObjectOutputStream ObjectInputStream InputStreamReader)
    (java.net SocketException)
    (org.apache.commons.net.ftp FTP FTPClient FTPFile)))


(def ^{:dynamic true} *client* {:ftp-client nil})


(def transfer-modes* {:binary (FTP/BINARY_FILE_TYPE) :ascii (FTP/ASCII_FILE_TYPE)})


(defn find-ftp-client*
  "Returns the current ftp client (or nil if there is none)"
  []
  (:ftp-client *client*))


(defn ftp-client*
  "Returns the current ftp client (or throws if there is none)"
  ^FTPClient []
  (or (find-ftp-client*)
      (throw (Exception. "no current database connection"))))


(defn get-ftp-client [{:keys [url user password site-commands transfer-mode]}]
  "Creates a new ftp client"
  (doto (FTPClient.)
    (.connect url)
    (.login user password)
    #_(.changeWorkingDirectory "''")
    (.setFileType (get transfer-modes* transfer-mode (:binary transfer-modes*)))))


(defn send-site-commands* [commands]
  (doseq [command commands]
    (.sendSiteCommand (ftp-client*) command)))


(defn with-ftpclient* [ftp-spec func]
  ""
  (let [ftp-client (get-ftp-client ftp-spec) 
        commands (seq (:site-commands ftp-spec))]
    (binding [*client* (assoc *client* :ftp-client ftp-client)]
      (send-site-commands* commands)
      (let [ret (func ftp-client)]
        (.logout ftp-client)
        (.disconnect ftp-client)
        ret))))

      
(defn exists-remotely?* [remote-file]
  (.retrieveFile (ftp-client*) remote-file (ByteArrayOutputStream.)))


(defn get-file* [remote-filename local-filename]
  (with-open [os (FileOutputStream. (File. local-filename))]
      (.retrieveFile (ftp-client*) remote-filename os)))


(defn put-stream* [remote-filename is]
    (if (exists-remotely?* remote-filename)
      (throw (IllegalArgumentException. "The file exists already!"))
      (.storeFile (ftp-client*) remote-filename is)))


(defn put-bytearray* [remote-filename byte-array]
  (with-open [is (ByteArrayInputStream. byte-array)]
    (put-stream* remote-filename is)))    


(defn put-file* [local-filename remote-filename]
  (with-open [is (FileInputStream. local-filename)]
      (put-stream* remote-filename is)))


(defn get-bytearray* [remote-filename]
  (with-open [os (ByteArrayOutputStream.)]
    (.retrieveFile (ftp-client*) remote-filename os)
    (.toByteArray os)))


(require '[clojure.java.io :as jio])


(defn- normalize-slurp-opts
  [opts]
  (if (string? (first opts))
    (do
      (println "WARNING: (slurp f enc) is deprecated, use (slurp f :encoding enc).")
      [:encoding (first opts)])
    opts))


(defn slurp* 
    "Opens a reader on f and reads all its contents, returning a string.
	  See clojure.java.io/reader for a complete list of supported arguments."
  {:added "1.0"}
  ([f & opts]
     (let [opts (normalize-slurp-opts opts)
           sb (StringBuilder.)]
       (with-open [#^java.io.BufferedReader r 
                   (apply jio/reader (.retrieveFileStream (ftp-client*) f) opts)]
         (loop [c (.read r)]
           (if (neg? c)
             (str sb)
             (do
               (.append sb (char c))
               (recur (.read r)))))))))


(defn spit*
  "Opposite of slurp.  Opens f with writer, writes content, then
  closes f. Options passed to clojure.java.io/writer."
  {:added "1.2"}
  [f content & options]
  (with-open [#^java.io.Writer w (apply jio/writer (.storeFileStream (ftp-client*) f) options)]
    (.write w (str content))))
