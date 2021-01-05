(ns example-static-website.core
  (:require [clojure.string :as str]
            [stasis.core :as stasis]
            [markdown.core :as md]
            [hiccup.page :as hiccup]))

(def source-dir "resources")

(defn key-to-html [s]
  (str/replace s #".md" ".html"))

(defn read-and-convert! [src]
  (let [data  (stasis/slurp-directory src #".*\.md$")
        html-paths (map key-to-html (keys data))
        html-content (map md/md-to-html-string (vals data))]
    (zipmap html-paths html-content)))


(defn apply-header-footer [page]
  (hiccup/html5 {:lang "en"}
                [:head
                 [:title "Static website!"]
                 [:meta {:charset "utf-8"}]
                 [:meta {:name "viewport"
                         :content "width=device-width, initial-scale=1.0"}]
                 [:body
                  [:div {:class "header"}
                   [:div {:class "name"}
                    [:a {:href "/"} "Home page"]
                    [:div {:class "header-right"}
                     [:a {:href "/posts"} "Posts"]]]]
                  page]
                 [:footer
                  [:p "This is the footer"]]]))

(apply-header-footer "Here is some content")

(defn format-pages [m]
  (let [html-keys (keys m)
        page-data (map apply-header-footer (vals m))]
    (zipmap html-keys page-data)))

(format-pages (read-and-convert! source-dir))
