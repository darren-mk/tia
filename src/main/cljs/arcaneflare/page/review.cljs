(ns arcaneflare.page.review
  (:require
   [arcaneflare.component.nav :as nav]))

(defn node []
  [:div 
   [nav/node]
   [:p "review page"]])

(comment 
  #_(defn item-page [ctx]
    (let [id (-> ctx :path-params :id)]
      [:div
       [:h2 "Selected item " id]])))