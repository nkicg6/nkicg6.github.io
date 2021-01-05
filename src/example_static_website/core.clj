(ns example-static-website.core
  (:require [clojure.string :as str]
            [stasis.core :as stasis]
            [markdown.core :as md]
            [hiccup.page :as hiccup]))

(def source-dir "resources")
(def out-dir "website")

(defn key-to-html [s]
  (str/replace s #".md" ".html"))

(defn read-and-convert! [src]
  (let [data  (stasis/slurp-directory src #".*\.md$")
        html-paths (map key-to-html (keys data))
        html-content (map md/md-to-html-string (vals data))]
    (zipmap html-paths html-content)))

(defn get-css [src]
  (stasis/slurp-directory src #".*\.css$"))

(defn apply-header-footer [page]
  (hiccup/html5 {:lang "en"}
                [:head
                 [:title "Static website!"]
                 [:meta {:charset "utf-8"}]
                 [:meta {:name "viewport"
                         :content "width=device-width, initial-scale=1.0"}]
                 [:link {:type "text/css" :href "/css/style.css" :rel "stylesheet"}]
                 [:body
                  [:div {:class "header"}
                   [:div {:class "name"}
                    [:a {:href "/"} "Home page"]
                    [:div {:class "header-right"}
                     [:a {:href "/posts"} "Posts"]]]]
                  page]
                 [:footer
                  [:p "This is the footer"]]]))

(defn format-pages [m]
  (let [html-keys (keys m)
        page-data (map apply-header-footer (vals m))]
    (zipmap html-keys page-data)))

(defn merge-website-assets! [root-dir]
  (let [page-map (format-pages (read-and-convert! root-dir))
        css-map (get-css source-dir)]
    (stasis/merge-page-sources {:css css-map
                                :pages page-map})))

(def server
  (stasis/serve-pages (merge-website-assets! source-dir)))

(defn export! []
  (stasis/empty-directory! out-dir)
  (stasis/export-pages (merge-website-assets! source-dir) out-dir)
  (println "Website is done!"))
