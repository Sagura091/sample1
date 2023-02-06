(ns sample1.core
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType]
    [markdown.core :refer [md->html]]
    [sample1.ajax :as ajax]
    [ajax.core :refer [GET POST]]
    [reitit.core :as reitit]
    [clojure.string :as string])
  (:import goog.History))

(defonce session (r/atom {:page :home}))
(defonce maths (r/atom [{}]))
(defn nav-link [uri title page]
  [:a.navbar-item
   {:href   uri
    :class (when (= page (:page @session)) "is-active")}
   title])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "sample1"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click #(swap! expanded? not)
        :class (when @expanded? :is-active)}
       [:span][:span][:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [nav-link "#/" "Home" :home]
       [nav-link "#/about" "About" :about]
       [nav-link "#/plus" "Addition" :plus]]]]))

(defn about-page []
  [:section.section>div.container>div.content
   [:img {:src "/img/warning_clojure.png"}]])

(defn get-data [path id]
   (GET (str path) :handler { :id id}))

(defn Display-Results [get-data]
  [:div
   [:form [:label :value "read"]]])

(def ^:dynamic total 1)

(defn home-page []
  [:section.section>div.container>div.content
   (when-let [docs (:docs @session)]
     [:div {:dangerouslySetInnerHTML {:__html (md->html docs)}}])])


(defn add-to-maths [form-data]
  (swap! maths conj  @form-data)
  (reset! form-data {}))

(defn input-yo [form-data result key-name]
  (println "before: " @form-data)
  (println result)
  (swap! form-data assoc key-name result)
  (println "After: " @form-data))

(defn handler [response])



(defn do-math [form-data]

   (println "do Math Before" @form-data)
  (GET "/api/math/plus"
       {:params  {:x (js/parseInt (get-in @form-data [:Num1])) :y (js/parseInt (get-in @form-data [:num2]))}
        :handler (fn [r]
                   (input-yo form-data (:total r) :total)
                   [:div
                    [:p (str (get-in @form-data [:total]))]]


                   (add-to-maths form-data))})

        

  (println "Now Calculating the Results")

  (println "After do math: " @form-data))
  ;(add-to-maths form-data))























(defn addition-page []
  (let[form-data (r/atom {})]
    (fn [numId]
      [:div {:class "opblock-tag-section is-open" :style {:color "pink"}}
       [:p (str @form-data)]
       [:input{
               :id 20
               :type "text"
               :on-change (fn [event] (input-yo form-data (-> event .-target .-value) :Num1))}]


       [:select {:id 5 :name "Selector" :style {:width 100}


                 :on-interface (fn [event] (input-yo form-data (-> event .-target .-value) :eq))
                 :on-change (fn [event] (input-yo form-data (-> event .-target .-value) :eq))}
        [:option {:value "+"} "+"]
        [:option {:value "-"} "-"]
        [:option {:value "*"} "*"]
        [:option {:value "/"} "/"]]


       [:input {
                :on-change (fn [event] (input-yo form-data (-> event .-target .-value) :num2))}]

       [:button {:style {:width 120 :height 20} :on-click #(do-math form-data)}]

       (let [x ()]
         (println x)
         [:p  (get-in (peek @maths) [:Num1]) (get-in (peek @maths) [:eq])  (get-in (peek @maths) [:num2]) " = " (get-in (peek @maths) [:total])])])))














(def pages
  {:home #'home-page
   :about #'about-page
   :plus #'addition-page})

(defn page []
  [(pages (:page @session))])

;; -------------------------
;; Routes

(def router
  (reitit/router
    [["/" :home]
     ["/about" :about]
     ["/plus" :plus]]))

(defn match-route [uri]
  (->> (or (not-empty (string/replace uri #"^.*#" "")) "/")
       (reitit/match-by-path router)
       :data
       :name))
;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [^js/Event.token event]
        (swap! session assoc :page (match-route (.-token event)))))
    (.setEnabled true)))


;; -------------------------
;; Initialize app
(defn fetch-docs! []
  (GET "/docs" {:handler #(swap! session assoc :docs %)}))

(defn ^:dev/after-load mount-components []
  (rdom/render [#'navbar] (.getElementById js/document "navbar"))
  (rdom/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (ajax/load-interceptors!)
  (fetch-docs!)
  (hook-browser-navigation!)
  (mount-components))
