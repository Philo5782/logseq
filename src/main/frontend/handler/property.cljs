(ns frontend.handler.property
  (:require [frontend.state :as state]
            [frontend.db :as db]
            [frontend.util :as util]
            [clojure.string :as string]))

(defn toggle-properties
  [id]
  (state/update-state! [:ui/properties-show? id] not))

(defn add-property!
  [block-db-id key]
  (let [block (db/pull block-db-id)
        key (string/trim key)
        key-name (util/page-name-sanity-lc key)
        property (db/entity [:block/name key-name])]
    (when-not (or
               (= (:block/name block) key-name)
               (and property
                    (or
                     (= (:block/type property) "tag")
                     (= (:db/id property) (:db/id block)))))
      (let [property-uuid (db/new-block-id)]
        (db/transact! (state/get-current-repo)
          [
           ;; property
           {:block/uuid property-uuid
            :block/type "property"
            :block/original-name key
            :block/name key-name}

           {:block/uuid (:block/uuid block)
            :block/properties (assoc (:block/properties block)
                                     property-uuid "")}])))))

(defn set-namespace!
  [page-db-id page-name]
  (let [page-name (util/page-name-sanity-lc page-name)
        page (db/entity [:block/name page-name])]
    (when page
      (db/transact! (state/get-current-repo)
        [{:db/id page-db-id
          :block/namespace (:db/id page)}]))))

(defn set-property-schema!
  [entity key]
  )

(defn add-property-value!
  [entity property-id property-value]
  (when-not (or
             (= property-id (:block/uuid entity))
             (string/blank? property-value))
    (db/transact! (state/get-current-repo)
      [{:block/uuid (:block/uuid entity)
        :block/properties (assoc (:block/properties entity) property-id property-value)}])))

(defn delete-property!
  [entity property-id]
  (when (and entity (uuid? property-id))
    (db/transact! (state/get-current-repo)
     [{:block/uuid (:block/uuid entity)
       :block/properties (dissoc (:block/properties entity) property-id)}])))