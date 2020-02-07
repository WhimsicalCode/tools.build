;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns clojure.tools.build
 (:require
   [clojure.java.io :as jio]
   [clojure.tools.deps.alpha :as deps]
   [clojure.tools.deps.alpha.reader :as reader]))

(def defaults
  "Build param defaults"
  {:build/target-dir "target"
   :build/clj-paths ["src"]
   :build/java-paths ["java" "src/main/java"]
   :build/resource-dirs ["resources"]
   :build/src-pom "pom.xml"})

(defn- look-up
  [deps-map alias-or-data]
  (if (keyword? alias-or-data)
    (get-in deps-map [:aliases alias-or-data])
    alias-or-data))

(defn build-info
  "Construct an initial build info. Optional kwargs:
    :deps Path to deps.edn file to use (\"deps.edn\" by default)
    :resolve Alias in deps.edn with resolve-deps args or a map of that data
    :params Alias in deps.edn with initial build params or a map of those params"
  [& {:keys [deps resolve params]
      :or {deps "deps.edn", params :build-info}}]
  (let [install-deps (reader/install-deps)
        user-dep-loc (jio/file (reader/user-deps-location))
        user-deps (when (.exists user-dep-loc) (reader/slurp-deps user-dep-loc))
        project-dep-loc (jio/file deps)
        project-deps (when (.exists project-dep-loc) (reader/slurp-deps project-dep-loc))
        deps-map (->> [install-deps user-deps project-deps] (remove nil?) reader/merge-deps)
        resolve-args (look-up deps-map resolve)
        lib-map (deps/resolve-deps deps-map nil nil)]
    {:lib-map lib-map
     :aliases (:aliases deps-map)
     :params (merge defaults (look-up deps-map params))}))

(comment
  (require '[clojure.tools.build.tasks :refer :all])

  ;; basic lib build
  (-> (build-info) clean sync-pom jar end)

  ;; basic app build

  (tasks/sync-pom (build-info))
  )
