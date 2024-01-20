(ns tia.db.person
  (:require
   [tia.db.common :as com]))

(defn nickname-existent? [s]
  (pos? (com/count-all-having-kv
         :person/nickname s)))

(comment
  (nickname-existent? "abc")
  :=> false)

(defn email-existent? [s]
  (pos? (com/count-all-having-kv
         :person/email s)))

(comment
  (email-existent? "abc@def.com")
  :=> false)

(defn create! [person]
  (com/record! person))

(comment
  (create! {:person/id #uuid "c864fd4b-ec4b-4310-8d69-e1be290cd57e"
            :person/nickname "jackiemema"
            :person/email "jackie@abc.com"
            :person/password "Abc123!@#"
            :person/role (keyword "customer")
            :person/agreed? (= "on" "on")})
  :=> #:xtdb.api{:tx-id 498,
                 :tx-time #inst "2024-01-20T20:03:50.401-00:00"})

(defn count-persons []
  (com/count-all-having-key :person/id))

(comment
  (count-persons)
  :=> 2)

(defn find-person-by-email [email]
  (ffirst
   (com/query
    '{:find [(pull ?person [*])]
      :in [[?email]]
      :where [[?person :person/email ?email]]}
    [email])))

(comment
  (find-person-by-email "kokonut@abc.com")
  :=> {:person/id #uuid "a3a9e552-773e-4b3b-9594-4c0fa5e6c79e",
       :person/nickname "kokonut",
       :person/email "kokonut@abc.com",
       :person/password "Abc123!@#",
       :person/role :customer,
       :person/agreed? true,
       :xt/id #uuid "a3a9e552-773e-4b3b-9594-4c0fa5e6c79e"})
