;; Copyright (c) Andreas Werner. All rights reserved. The use and
;; distribution terms for this software are covered by the Eclipse Public
;; License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
;; be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other,
;; from this software.
;;
;; core.clj
;;
;; A clojure interface to FTP via the apache commons-net ftp client
;;
;; werner_andreas (@web.de)
;; Created 18 March 2012
(ns
  ^{
    :author "Andreas Werner",
    :doc "A clojure interface to FTP via the apache commons-net ftp client"}  
  ftp.core  
  (:import 
    (org.apache.commons.net.ftp FTPClient))
  (:use ftp.internal.internal))


;; Dokumentation der Transfer-Modes


(defmacro with-ftp
  [ftp-spec & body]
  `(with-ftpclient* ~ftp-spec (fn [~'unnamed] ~@body)))


(defmacro with-ftp-client
  [^FTPClient ftp-client-binding ftp-spec & body]
  `(with-ftpclient* ~ftp-spec (fn [~ftp-client-binding] ~@body)))


(defn exists-remotely? [remote-file]
    (exists-remotely?* remote-file))
  

(defn get-file [remote-filename local-filename]
  (get-file* remote-filename local-filename))


(defn put-bytearray [remote-filename byte-array]
  (put-bytearray* remote-filename byte-array))


(defn put-stream [remote-filename is]
  (put-stream* remote-filename is))


(defn put-file [local-filename remote-filename]
  (put-file* local-filename remote-filename))


(defn get-bytearray [remote-filename]
  (get-bytearray* remote-filename))


(defn slurp [remote-filename & opts]
  (apply slurp* remote-filename opts))


(defn spit [remote-filename content & opts]
  (apply spit* remote-filename content opts))
