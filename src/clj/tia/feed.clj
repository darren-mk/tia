(ns tia.feed
  (:require
   [clojure.string :as cstr]
   [clj-http.client :as client]
   [cheshire.core :as json]
   [malli.core :as m]
   [mount.core :refer [defstate]]
   [tia.calc :as calc]
   [tia.config :as config]
   [tia.data :as data]
   [tia.db.common :as dbc]
   [tia.model :as model]
   [tia.util :as u]))

(declare gplace-api-key)

(defstate ^:dynamic gplace-api-key
  :start (:gmap-api-key config/env)
  :stop nil)

(def gplace-fields
  #{:googleMapsUri :internationalPhoneNumber
    :displayName :websiteUri :businessStatus
    :id :formattedAddress
    
    :addressComponents
    :adrFormatAddress
    :shortFormattedAddress})

(defn mask-gplace [fields]
  (->> fields
       (map #(str "places." (name %)))
       (cstr/join ",")))

(defn request [text-query]
  (let [mask (mask-gplace gplace-fields)
        payload {:save-request? true
                 :headers {"Content-Type" "application/json"
                           "X-Goog-FieldMask" mask
                           "X-Goog-Api-Key" gplace-api-key}
                 :form-params {:textQuery text-query}}]
    (-> (client/post data/gplace-uri payload) :body
      #_#_  (json/parse-string keyword) :places)))

(def a (request "strip clubs in Bergen County, New Jersey"))

(comment
  (-> a
      (json/parse-string keyword))
  ;; => {:places
  ;;     [{:googleMapsUri
  ;;       "https://maps.google.com/?cid=15978652145520910338",
  ;;       :internationalPhoneNumber "+1 201-880-7667",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">125 Saddle River Ave</span>, <span class=\"locality\">South Hackensack</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07606</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Blush Gentlemen's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJjUYyXxb5wokRAqBOw3eTv90",
  ;;       :websiteUri "http://www.blushgc.com/",
  ;;       :shortFormattedAddress "125 Saddle River Ave, South Hackensack",
  ;;       :formattedAddress
  ;;       "125 Saddle River Ave, South Hackensack, NJ 07606, USA",
  ;;       :addressComponents
  ;;       [{:longText "125",
  ;;         :shortText "125",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Saddle River Avenue",
  ;;         :shortText "Saddle River Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "South Hackensack",
  ;;         :shortText "South Hackensack",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Bergen County",
  ;;         :shortText "Bergen County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07606",
  ;;         :shortText "07606",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=5436509226770822902",
  ;;       :internationalPhoneNumber "+1 201-440-3332",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">430 US-46</span>, <span class=\"locality\">South Hackensack</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07606</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Flamingo Gentlemen's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJu9yz8sb5wokR9vbOf1Zccks",
  ;;       :websiteUri "http://www.clubflamingonj.com/",
  ;;       :shortFormattedAddress "430 US-46, South Hackensack",
  ;;       :formattedAddress "430 US-46, South Hackensack, NJ 07606, USA",
  ;;       :addressComponents
  ;;       [{:longText "430",
  ;;         :shortText "430",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "U.S. 46",
  ;;         :shortText "US-46",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "South Hackensack",
  ;;         :shortText "South Hackensack",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Bergen County",
  ;;         :shortText "Bergen County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07606",
  ;;         :shortText "07606",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=9967715053334798380",
  ;;       :internationalPhoneNumber "+1 201-226-9300",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">100 NJ-17</span>, <span class=\"locality\">Lodi</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07644</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "The Harem Cabaret", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJD5suWO75wokRLKBVoAdwVIo",
  ;;       :websiteUri "http://facebook.com/theharemcabaretnj",
  ;;       :shortFormattedAddress "100 NJ-17, Lodi",
  ;;       :formattedAddress "100 NJ-17, Lodi, NJ 07644, USA",
  ;;       :addressComponents
  ;;       [{:longText "100",
  ;;         :shortText "100",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "New Jersey 17",
  ;;         :shortText "NJ-17",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Lodi",
  ;;         :shortText "Lodi",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Bergen County",
  ;;         :shortText "Bergen County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07644",
  ;;         :shortText "07644",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=16690990646616337660",
  ;;       :internationalPhoneNumber "+1 201-880-6254",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">213 Huyler St</span>, <span class=\"locality\">South Hackensack</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07606-1302</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Players Club & Grill", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJKXmAi-35wokR_CzveI9Pouc",
  ;;       :websiteUri
  ;;       "https://www.facebook.com/The-Players-Club-NJ-211025376318280/",
  ;;       :shortFormattedAddress "213 Huyler St, South Hackensack",
  ;;       :formattedAddress
  ;;       "213 Huyler St, South Hackensack, NJ 07606, USA",
  ;;       :addressComponents
  ;;       [{:longText "213",
  ;;         :shortText "213",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Huyler Street",
  ;;         :shortText "Huyler St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "South Hackensack",
  ;;         :shortText "South Hackensack",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Bergen County",
  ;;         :shortText "Bergen County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07606",
  ;;         :shortText "07606",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "1302",
  ;;         :shortText "1302",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=17772163824878871965",
  ;;       :internationalPhoneNumber "+1 201-909-8983",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">230 NJ-17</span>, <span class=\"locality\">Lodi</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07644-3823</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Satin Dolls", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJAW4yR4b5wokRneXzd9Zoo_Y",
  ;;       :shortFormattedAddress "230 NJ-17, Lodi",
  ;;       :formattedAddress "230 NJ-17, Lodi, NJ 07644, USA",
  ;;       :addressComponents
  ;;       [{:longText "230",
  ;;         :shortText "230",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "New Jersey 17",
  ;;         :shortText "NJ-17",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Lodi",
  ;;         :shortText "Lodi",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Bergen County",
  ;;         :shortText "Bergen County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07644",
  ;;         :shortText "07644",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "3823",
  ;;         :shortText "3823",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=9453108450999506705",
  ;;       :internationalPhoneNumber "+1 973-279-1200",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">556 Straight St</span>, <span class=\"locality\">Paterson</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07503</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Sunrise Sports Go Go Strip Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ6wBWQCz8wokREZcRLQ0wMIM",
  ;;       :websiteUri "http://www.sunrisegogo.com/",
  ;;       :shortFormattedAddress "556 Straight St, Paterson",
  ;;       :formattedAddress "556 Straight St, Paterson, NJ 07503, USA",
  ;;       :addressComponents
  ;;       [{:longText "556",
  ;;         :shortText "556",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Straight Street",
  ;;         :shortText "Straight St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Paterson",
  ;;         :shortText "Paterson",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07503",
  ;;         :shortText "07503",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:id "ChIJoWR_68T9wokRVD-szW_GC1Q",
  ;;       :formattedAddress "10 1st Ave, Paterson, NJ 07524, USA",
  ;;       :addressComponents
  ;;       [{:longText "10",
  ;;         :shortText "10",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "1st Avenue",
  ;;         :shortText "1st Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Paterson",
  ;;         :shortText "Paterson",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07524",
  ;;         :shortText "07524",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}],
  ;;       :googleMapsUri "https://maps.google.com/?cid=6056152307424051028",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">10 1st Ave</span>, <span class=\"locality\">Paterson</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07524</span>, <span class=\"country-name\">USA</span>",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Silk city Gentlemen's Lounge", :languageCode "en"},
  ;;       :shortFormattedAddress "10 1st Ave, Paterson"}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=14480630176803765776",
  ;;       :internationalPhoneNumber "+1 973-779-7455",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">426 Van Houten Ave</span>, <span class=\"locality\">Passaic</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07055</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Platinum Dollz Gentlemens Lounge", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ0XE9giD_wokREL4wlGyI9cg",
  ;;       :websiteUri "http://instagram.com/platinumdollzpassaicnj",
  ;;       :shortFormattedAddress "426 Van Houten Ave, Passaic",
  ;;       :formattedAddress "426 Van Houten Ave, Passaic, NJ 07055, USA",
  ;;       :addressComponents
  ;;       [{:longText "426",
  ;;         :shortText "426",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Van Houten Avenue",
  ;;         :shortText "Van Houten Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic",
  ;;         :shortText "Passaic",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07055",
  ;;         :shortText "07055",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=16418971822761704872",
  ;;       :internationalPhoneNumber "+1 201-531-0493",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">325 Paterson Plank Rd</span>, <span class=\"locality\">East Rutherford</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07073</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Stiletto", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJJ8wNqGb4wokRqP3boOHn2-M",
  ;;       :websiteUri "http://www.stilettojersey.com/",
  ;;       :shortFormattedAddress "325 Paterson Plank Rd, East Rutherford",
  ;;       :formattedAddress
  ;;       "325 Paterson Plank Rd, East Rutherford, NJ 07073, USA",
  ;;       :addressComponents
  ;;       [{:longText "325",
  ;;         :shortText "325",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Paterson Plank Road",
  ;;         :shortText "Paterson Plank Rd",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "East Rutherford",
  ;;         :shortText "East Rutherford",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Bergen County",
  ;;         :shortText "Bergen County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07073",
  ;;         :shortText "07073",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=7775269342707746748",
  ;;       :internationalPhoneNumber "+1 973-365-0373",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">40 Brighton Ave</span>, <span class=\"locality\">Passaic</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07055-2031</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Silk Gentlemens Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJvcuTEGX_wokRvIcUpUVO52s",
  ;;       :websiteUri "http://www.silkgentlemensclub.com/",
  ;;       :shortFormattedAddress "40 Brighton Ave, Passaic",
  ;;       :formattedAddress "40 Brighton Ave, Passaic, NJ 07055, USA",
  ;;       :addressComponents
  ;;       [{:longText "40",
  ;;         :shortText "40",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Brighton Avenue",
  ;;         :shortText "Brighton Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic",
  ;;         :shortText "Passaic",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07055",
  ;;         :shortText "07055",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "2031",
  ;;         :shortText "2031",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=16275038674271375188",
  ;;       :internationalPhoneNumber "+1 973-653-5615",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">168 Getty Ave</span>, <span class=\"locality\">Paterson</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07503-2808</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Tease", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJW5jvTCj8wokRVI-wBXKN3OE",
  ;;       :shortFormattedAddress "168 Getty Ave, Paterson",
  ;;       :formattedAddress "168 Getty Ave, Paterson, NJ 07503, USA",
  ;;       :addressComponents
  ;;       [{:longText "168",
  ;;         :shortText "168",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Getty Avenue",
  ;;         :shortText "Getty Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Paterson",
  ;;         :shortText "Paterson",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07503",
  ;;         :shortText "07503",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "2808",
  ;;         :shortText "2808",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=4190522981939401909",
  ;;       :internationalPhoneNumber "+1 973-684-7678",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">95 Barclay St</span>, <span class=\"locality\">Paterson</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07503</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Johnny A's Hitching Post", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJicTKlir8wokRtdSbIXO6Jzo",
  ;;       :websiteUri "http://www.johnnyashitchingpost.com/",
  ;;       :shortFormattedAddress "95 Barclay St, Paterson",
  ;;       :formattedAddress "95 Barclay St, Paterson, NJ 07503, USA",
  ;;       :addressComponents
  ;;       [{:longText "95",
  ;;         :shortText "95",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Barclay Street",
  ;;         :shortText "Barclay St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Paterson",
  ;;         :shortText "Paterson",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07503",
  ;;         :shortText "07503",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=6188957603695296192",
  ;;       :internationalPhoneNumber "+1 973-279-6999",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">324 E Railway Ave</span>, <span class=\"locality\">Paterson</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07503</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Roxxies Night Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJbTaKwJ7-wokRwP41aiaY41U",
  ;;       :shortFormattedAddress "324 E Railway Ave, Paterson",
  ;;       :formattedAddress "324 E Railway Ave, Paterson, NJ 07503, USA",
  ;;       :addressComponents
  ;;       [{:longText "324",
  ;;         :shortText "324",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "East Railway Avenue",
  ;;         :shortText "E Railway Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Paterson",
  ;;         :shortText "Paterson",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07503",
  ;;         :shortText "07503",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=17776205003883439903",
  ;;       :internationalPhoneNumber "+1 973-423-0499",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">105 Mohawk Ave</span>, <span class=\"locality\">Hawthorne</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07506-3737</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Jiggles", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJDf1GEv_8wokRH2f-xkTEsfY",
  ;;       :shortFormattedAddress "105 Mohawk Ave, Hawthorne",
  ;;       :formattedAddress "105 Mohawk Ave, Hawthorne, NJ 07506, USA",
  ;;       :addressComponents
  ;;       [{:longText "105",
  ;;         :shortText "105",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Mohawk Avenue",
  ;;         :shortText "Mohawk Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Ashley Heights",
  ;;         :shortText "Ashley Heights",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Hawthorne",
  ;;         :shortText "Hawthorne",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07506",
  ;;         :shortText "07506",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "3737",
  ;;         :shortText "3737",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=4473982938725009753",
  ;;       :internationalPhoneNumber "+1 973-684-9589",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">821 McBride Ave</span>, <span class=\"locality\">Woodland Park</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07424-2701</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Ragtime Gentlemen's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJh1yNIwz-wokRWcncUcfHFj4",
  ;;       :shortFormattedAddress "821 McBride Ave, Woodland Park",
  ;;       :formattedAddress "821 McBride Ave, Woodland Park, NJ 07424, USA",
  ;;       :addressComponents
  ;;       [{:longText "821",
  ;;         :shortText "821",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "McBride Avenue",
  ;;         :shortText "McBride Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Woodland Park",
  ;;         :shortText "Woodland Park",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07424",
  ;;         :shortText "07424",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "2701",
  ;;         :shortText "2701",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=8669375301418381111",
  ;;       :internationalPhoneNumber "+1 973-345-0305",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">247 Grand St</span>, <span class=\"locality\">Paterson</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07501-2706</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Doctor's Cave Gentlemen's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJjbd-5NH9wokRN2-WgefOT3g",
  ;;       :shortFormattedAddress "247 Grand St, Paterson",
  ;;       :formattedAddress "247 Grand St, Paterson, NJ 07501, USA",
  ;;       :addressComponents
  ;;       [{:longText "247",
  ;;         :shortText "247",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Grand Street",
  ;;         :shortText "Grand St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Paterson",
  ;;         :shortText "Paterson",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07501",
  ;;         :shortText "07501",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "2706",
  ;;         :shortText "2706",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=15802214468416290347",
  ;;       :internationalPhoneNumber "+1 973-333-5570",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">108 Market St</span>, <span class=\"locality\">Paterson</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07505</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Xscape GoGo", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ_5qZdsv9wokRK7591lW-TNs",
  ;;       :websiteUri "https://www.xscapegogo.com/",
  ;;       :shortFormattedAddress "108 Market St, Paterson",
  ;;       :formattedAddress "108 Market St, Paterson, NJ 07505, USA",
  ;;       :addressComponents
  ;;       [{:longText "108",
  ;;         :shortText "108",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Market Street",
  ;;         :shortText "Market St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Paterson",
  ;;         :shortText "Paterson",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07505",
  ;;         :shortText "07505",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=4174460611856921043",
  ;;       :internationalPhoneNumber "+1 201-342-6410",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">77 Kinderkamack Rd</span>, <span class=\"locality\">River Edge</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07661</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Club Feathers", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJKUlUHHr6wokR03GXKs-p7jk",
  ;;       :websiteUri "http://www.clubfeathersnj.com/",
  ;;       :shortFormattedAddress "77 Kinderkamack Rd, River Edge",
  ;;       :formattedAddress "77 Kinderkamack Rd, River Edge, NJ 07661, USA",
  ;;       :addressComponents
  ;;       [{:longText "77",
  ;;         :shortText "77",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Kinderkamack Road",
  ;;         :shortText "Kinderkamack Rd",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "River Edge",
  ;;         :shortText "River Edge",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Bergen County",
  ;;         :shortText "Bergen County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07661",
  ;;         :shortText "07661",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=8038200693686595764",
  ;;       :internationalPhoneNumber "+1 973-279-1292",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">182 Getty Ave</span>, <span class=\"locality\">Paterson</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07503-2809</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Hi-Beams Go-Go Lounge", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJx3cBSij8wokRtPB8VPFsjW8",
  ;;       :websiteUri "http://www.hibeamsgogo.com/",
  ;;       :shortFormattedAddress "182 Getty Ave, Paterson",
  ;;       :formattedAddress "182 Getty Ave, Paterson, NJ 07503, USA",
  ;;       :addressComponents
  ;;       [{:longText "182",
  ;;         :shortText "182",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Getty Avenue",
  ;;         :shortText "Getty Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Paterson",
  ;;         :shortText "Paterson",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07503",
  ;;         :shortText "07503",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "2809",
  ;;         :shortText "2809",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=3115326030898632768",
  ;;       :internationalPhoneNumber "+1 973-256-3302",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">24 Galesi Dr</span>, <span class=\"locality\">Wayne</span>, <span class=\"region\">NJ</span> <span class=\"postal-code\">07470-4805</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Lace", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJNcw5nZMBw4kRQJg994TcOys",
  ;;       :websiteUri "http://www.lacewaynenj.com/",
  ;;       :shortFormattedAddress "24 Galesi Dr, Wayne",
  ;;       :formattedAddress "24 Galesi Dr, Wayne, NJ 07470, USA",
  ;;       :addressComponents
  ;;       [{:longText "24",
  ;;         :shortText "24",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Galesi Drive",
  ;;         :shortText "Galesi Dr",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Wayne",
  ;;         :shortText "Wayne",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Passaic County",
  ;;         :shortText "Passaic County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New Jersey",
  ;;         :shortText "NJ",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "07470",
  ;;         :shortText "07470",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "4805",
  ;;         :shortText "4805",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}]}
  ;; => {:places
  ;;     [{:googleMapsUri "https://maps.google.com/?cid=8445145318900983358",
  ;;       :internationalPhoneNumber "+1 877-497-8747",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">C. Coahuila 2009</span>, <span class=\"extended-address\">Zona Nte.</span>, <span class=\"postal-code\">22000</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :displayName
  ;;       {:text "Hong Kong Gentlemen’s Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJuXfDlAZJ2YARPmalWewuM3U",
  ;;       :websiteUri "http://hktijuana.com/",
  ;;       :shortFormattedAddress "C. Coahuila 2009, Zona Nte., Tijuana",
  ;;       :formattedAddress
  ;;       "C. Coahuila 2009, Zona Nte., 22000 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "2009",
  ;;         :shortText "2009",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Calle Coahuila",
  ;;         :shortText "C. Coahuila",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Zona Norte",
  ;;         :shortText "Zona Nte.",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22000",
  ;;         :shortText "22000",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=12259017298200300819",
  ;;       :internationalPhoneNumber "+1 877-761-7077",
  ;;       :adrFormatAddress
  ;;       "Pueblo Amigo, <span class=\"street-address\">Vía Ote. 9211</span>, <span class=\"extended-address\">Zona Urbana Rio Tijuana</span>, <span class=\"postal-code\">22320</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :displayName
  ;;       {:text "Deja Vu Showgirls Tijuana", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJw-O4rPhI2YAREw2ZGkjHIKo",
  ;;       :websiteUri "http://www.dejavutijuana.com/",
  ;;       :shortFormattedAddress
  ;;       "Pueblo Amigo, Vía Ote. 9211, Zona Urbana Rio Tijuana, Tijuana",
  ;;       :formattedAddress
  ;;       "Pueblo Amigo, Vía Ote. 9211, Zona Urbana Rio Tijuana, 22320 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "Pueblo Amigo",
  ;;         :shortText "Pueblo Amigo",
  ;;         :types ["point_of_interest" "establishment"],
  ;;         :languageCode "es"}
  ;;        {:longText "9211",
  ;;         :shortText "9211",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Vía Oriente",
  ;;         :shortText "Vía Ote.",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Zona Urbana Rio Tijuana",
  ;;         :shortText "Zona Urbana Rio Tijuana",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22320",
  ;;         :shortText "22320",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=5926059903019887895",
  ;;       :internationalPhoneNumber "+52 664 307 9924",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">Av Constitución 414</span>, <span class=\"extended-address\">Zona Nte.</span>, <span class=\"postal-code\">22000</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :displayName
  ;;       {:text "Chicago Gentlemen’s Club", :languageCode "es"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJqd46twZJ2YARF60vjRWYPVI",
  ;;       :websiteUri "http://chicagoclubtijuana.com/",
  ;;       :shortFormattedAddress "Av Constitución 414, Zona Nte., Tijuana",
  ;;       :formattedAddress
  ;;       "Av Constitución 414, Zona Nte., 22000 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "414",
  ;;         :shortText "414",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Avenida Constitución",
  ;;         :shortText "Av Constitución",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Zona Norte",
  ;;         :shortText "Zona Nte.",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22000",
  ;;         :shortText "22000",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=2904774959301691054",
  ;;       :internationalPhoneNumber "+52 664 581 8840",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">Blvd. Gustavo Díaz Ordaz 1195</span>, <span class=\"extended-address\">Dimenstein</span>, <span class=\"postal-code\">22105</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :displayName {:text "La Cueva del Peludo", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJA1wKxINJ2YARroagcW_VTyg",
  ;;       :websiteUri "http://www.lacuevabar.com/",
  ;;       :shortFormattedAddress
  ;;       "Blvd. Gustavo Díaz Ordaz 1195, Dimenstein, Tijuana",
  ;;       :formattedAddress
  ;;       "Blvd. Gustavo Díaz Ordaz 1195, Dimenstein, 22105 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "1195",
  ;;         :shortText "1195",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulevard Gustavo Díaz Ordaz",
  ;;         :shortText "Blvd. Gustavo Díaz Ordaz",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Dimenstein",
  ;;         :shortText "Dimenstein",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22105",
  ;;         :shortText "22105",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=1242582634478116044",
  ;;       :internationalPhoneNumber "+52 877 497 8747",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">C. Coahuila 2009</span>, <span class=\"extended-address\">Zona Nte.</span>, <span class=\"postal-code\">22000</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :displayName {:text "Las Chavelas Bar", :languageCode "es"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ6RYClQZJ2YARzBwjlFKKPhE",
  ;;       :websiteUri "http://laschavelasbar.com/",
  ;;       :shortFormattedAddress "C. Coahuila 2009, Zona Nte., Tijuana",
  ;;       :formattedAddress
  ;;       "C. Coahuila 2009, Zona Nte., 22000 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "2009",
  ;;         :shortText "2009",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Calle Coahuila",
  ;;         :shortText "C. Coahuila",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Zona Norte",
  ;;         :shortText "Zona Nte.",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22000",
  ;;         :shortText "22000",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=10605463295117371564",
  ;;       :internationalPhoneNumber "+52 664 875 2295",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">C. Coahuila 1841</span>, <span class=\"extended-address\">Zona Nte.</span>, <span class=\"postal-code\">21910</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :displayName {:text "Adelita Bar", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJH59bbQZJ2YARrCjqP60sLpM",
  ;;       :websiteUri "https://adelitabarmx.com/",
  ;;       :shortFormattedAddress "C. Coahuila 1841, Zona Nte., Tijuana",
  ;;       :formattedAddress
  ;;       "C. Coahuila 1841, Zona Nte., 21910 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "1841",
  ;;         :shortText "1841",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Calle Coahuila",
  ;;         :shortText "C. Coahuila",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Zona Norte",
  ;;         :shortText "Zona Nte.",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "21910",
  ;;         :shortText "21910",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:id "ChIJzxgBy0RJ2YARHq0z5FjOIGY",
  ;;       :formattedAddress
  ;;       "C. Coahuila 8075, Zona Nte., 22000 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "8075",
  ;;         :shortText "8075",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Calle Coahuila",
  ;;         :shortText "C. Coahuila",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Zona Norte",
  ;;         :shortText "Zona Nte.",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22000",
  ;;         :shortText "22000",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}],
  ;;       :googleMapsUri "https://maps.google.com/?cid=7359108672304426270",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">C. Coahuila 8075</span>, <span class=\"extended-address\">Zona Nte.</span>, <span class=\"postal-code\">22000</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "Bar Gold Palace", :languageCode "en"},
  ;;       :shortFormattedAddress "C. Coahuila 8075, Zona Nte., Tijuana"}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=13621895101700102232",
  ;;       :internationalPhoneNumber "+52 664 492 3011",
  ;;       :adrFormatAddress
  ;;       "Ave. Revolucion, esquina con, <span class=\"street-address\">Calle Ignacio Zaragoza 1300</span>, <span class=\"extended-address\">Zona Centro</span>, <span class=\"postal-code\">22000</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :displayName {:text "Amnesia Tijuana", :languageCode "es"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJq6qqGkdI2YARWNh3UGyxCr0",
  ;;       :shortFormattedAddress
  ;;       "esquina con, Ave. Revolucion, Calle Ignacio Zaragoza 1300, Tijuana",
  ;;       :formattedAddress
  ;;       "Ave. Revolucion, esquina con, Calle Ignacio Zaragoza 1300, Zona Centro, 22000 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "esquina con",
  ;;         :shortText "esquina con",
  ;;         :types ["point_of_interest" "establishment"],
  ;;         :languageCode "es"}
  ;;        {:longText "1300",
  ;;         :shortText "1300",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Calle Ignacio Zaragoza",
  ;;         :shortText "Calle Ignacio Zaragoza",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Zona Centro",
  ;;         :shortText "Zona Centro",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22000",
  ;;         :shortText "22000",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=16385039325316109865",
  ;;       :internationalPhoneNumber "+52 664 357 3947",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">Av. Revolución 601</span>, <span class=\"extended-address\">Zona Nte.</span>, <span class=\"postal-code\">22000</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :displayName {:text "Premier Men's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJQR7hJQFJ2YARKdZFaHRaY-M",
  ;;       :shortFormattedAddress "Av. Revolución 601, Zona Nte., Tijuana",
  ;;       :formattedAddress
  ;;       "Av. Revolución 601, Zona Nte., 22000 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "601",
  ;;         :shortText "601",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Avenida Revolución",
  ;;         :shortText "Av. Revolución",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Zona Norte",
  ;;         :shortText "Zona Nte.",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22000",
  ;;         :shortText "22000",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=1304774392389211625",
  ;;       :internationalPhoneNumber "+52 664 688 2036",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">C. 8va. Miguel Hidalgo 7903</span>, <span class=\"extended-address\">Centro</span>, <span class=\"postal-code\">22000</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :displayName {:text "New Body", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJg-3YKalJ2YAR6ZVimmV9GxI",
  ;;       :shortFormattedAddress "C. 8a. Hidalgo 7903, Centro, Tijuana",
  ;;       :formattedAddress
  ;;       "C. 8va. Miguel Hidalgo 7903, Centro, 22000 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "7903",
  ;;         :shortText "7903",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Calle 8va. Miguel Hidalgo",
  ;;         :shortText "Calle 8va. Miguel Hidalgo",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Centro",
  ;;         :shortText "Centro",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22000",
  ;;         :shortText "22000",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=10510202263212776596",
  ;;       :internationalPhoneNumber "+52 664 685 5519",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">Av Constitución 191</span>, <span class=\"extended-address\">Zona Nte.</span>, <span class=\"postal-code\">22000</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :displayName {:text "La gloria bar", :languageCode "es"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJLyF4yAZJ2YARlNxN4EW925E",
  ;;       :shortFormattedAddress "Av Constitución 191, Zona Nte., Tijuana",
  ;;       :formattedAddress
  ;;       "Av Constitución 191, Zona Nte., 22000 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "191",
  ;;         :shortText "191",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Avenida Constitución",
  ;;         :shortText "Av Constitución",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Zona Norte",
  ;;         :shortText "Zona Nte.",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22000",
  ;;         :shortText "22000",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:id "ChIJRTR4vwZJ2YARUIiQI6h1HkU",
  ;;       :formattedAddress
  ;;       "C. Coahuila 8084, International Gateway Of The Americas, Zona Nte., 92173 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "8084",
  ;;         :shortText "8084",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Calle Coahuila",
  ;;         :shortText "C. Coahuila",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "International Gateway Of The Americas",
  ;;         :shortText "International Gateway Of The Americas",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Zona Norte",
  ;;         :shortText "Zona Nte.",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "92173",
  ;;         :shortText "92173",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}],
  ;;       :googleMapsUri "https://maps.google.com/?cid=4980547602929977424",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">C. Coahuila 8084</span>, <span class=\"extended-address\">International Gateway Of The Americas, Zona Nte.</span>, <span class=\"postal-code\">92173</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "Adelita Store", :languageCode "en"},
  ;;       :shortFormattedAddress
  ;;       "C. Coahuila 8084, International Gateway Of The Americas, Zona Nte., Tijuana"}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=8822788032507301165",
  ;;       :internationalPhoneNumber "+52 664 688 3549",
  ;;       :adrFormatAddress
  ;;       "Calle 8 y Avenida Revolución 1220, <span class=\"extended-address\">Zona Centro</span>, <span class=\"postal-code\">22000</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :displayName
  ;;       {:text "Secret's Gentlemen's Bar", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJN9ChSZZJ2YARLU0qbvjWcHo",
  ;;       :shortFormattedAddress
  ;;       "Calle 8 y Avenida Revolución 1220, Zona Centro, Tijuana",
  ;;       :formattedAddress
  ;;       "Calle 8 y Avenida Revolución 1220, Zona Centro, 22000 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "Zona Centro",
  ;;         :shortText "Zona Centro",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22000",
  ;;         :shortText "22000",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=16645879361865024333",
  ;;       :internationalPhoneNumber "+52 664 622 6600",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">Blvd. Agua Caliente No 11553</span>, <span class=\"extended-address\">Aviacion</span>, <span class=\"postal-code\">22014</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :displayName {:text "Exclusive Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJVVVlnRpI2YARTYP4ARYLAuc",
  ;;       :websiteUri
  ;;       "http://www.exclusiveclub.com.mx/mailmkt/2012/tijuana/14feb2/flyer.html",
  ;;       :shortFormattedAddress
  ;;       "Blvd. Agua Caliente No 11553, Aviacion, Tijuana",
  ;;       :formattedAddress
  ;;       "Blvd. Agua Caliente No 11553, Aviacion, 22014 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "No 11553",
  ;;         :shortText "No 11553",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulevard Agua Caliente",
  ;;         :shortText "Blvd. Agua Caliente",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Aviacion",
  ;;         :shortText "Aviacion",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22014",
  ;;         :shortText "22014",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:id "ChIJZaZrSlM_2YAR-wW71vg-IRY",
  ;;       :formattedAddress
  ;;       "Los Lirios, Jardin Dorado, 22200 Tijuana, B.C., Mexico",
  ;;       :addressComponents
  ;;       [{:longText "Los Lirios",
  ;;         :shortText "Los Lirios",
  ;;         :types ["route"],
  ;;         :languageCode "es"}
  ;;        {:longText "Jardin Dorado",
  ;;         :shortText "Jardin Dorado",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Tijuana",
  ;;         :shortText "Tijuana",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Baja California",
  ;;         :shortText "B.C.",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "es"}
  ;;        {:longText "Mexico",
  ;;         :shortText "MX",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "22200",
  ;;         :shortText "22200",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}],
  ;;       :googleMapsUri "https://maps.google.com/?cid=1594624981541258747",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">Los Lirios</span>, <span class=\"extended-address\">Jardin Dorado</span>, <span class=\"postal-code\">22200</span> <span class=\"locality\">Tijuana</span>, <span class=\"region\">B.C.</span>, <span class=\"country-name\">Mexico</span>",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Fausto's Place and Mermaids", :languageCode "en"},
  ;;       :shortFormattedAddress "Los Lirios, Jardin Dorado, Tijuana"}]}
  ;; => {:places
  ;;     [{:googleMapsUri
  ;;       "https://maps.google.com/?cid=15731930692466354764",
  ;;       :internationalPhoneNumber "+1 514-934-3329",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3802 R. Notre Dame O</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H4C 1P9</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Montreal Strip Club", :languageCode "fr"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ3frKYp0QyUwRTKqFC50LU9o",
  ;;       :shortFormattedAddress "3802 Notre-Dame St W, Montreal",
  ;;       :formattedAddress
  ;;       "3802 R. Notre Dame O, Montréal, QC H4C 1P9, Canada",
  ;;       :addressComponents
  ;;       [{:longText "3802",
  ;;         :shortText "3802",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Notre-Dame Ouest",
  ;;         :shortText "R. Notre Dame O",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Le Sud-Ouest",
  ;;         :shortText "Le Sud-Ouest",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H4C 1P9",
  ;;         :shortText "H4C 1P9",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=16866874346928347528",
  ;;       :internationalPhoneNumber "+1 514-842-4892",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3580 Rue Saint-Dominique</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2X 2X4</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Kamasutra Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJA7XrhEoayUwRiBnQn9osE-o",
  ;;       :websiteUri "http://www.kamasutramtl.com/",
  ;;       :shortFormattedAddress "3580 Saint Dominique St., Montreal",
  ;;       :formattedAddress
  ;;       "3580 Rue Saint-Dominique, Montréal, QC H2X 2X4, Canada",
  ;;       :addressComponents
  ;;       [{:longText "3580",
  ;;         :shortText "3580",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Saint-Dominique",
  ;;         :shortText "Rue Saint-Dominique",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Le Plateau-Mont-Royal",
  ;;         :shortText "Le Plateau-Mont-Royal",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2X 2X4",
  ;;         :shortText "H2X 2X4",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=15456279697285596594",
  ;;       :internationalPhoneNumber "+1 514-286-1417",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1417 Boul. Saint-Laurent</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2X 2S8</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Cabaret Kingdom", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ-yeH1U0ayUwRskXKaX68f9Y",
  ;;       :websiteUri "https://www.kingdommontreal.com/",
  ;;       :shortFormattedAddress "1417 St Laurent Blvd, Montreal",
  ;;       :formattedAddress
  ;;       "1417 Boul. Saint-Laurent, Montréal, QC H2X 2S8, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1417",
  ;;         :shortText "1417",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulevard Saint-Laurent",
  ;;         :shortText "Boul. Saint-Laurent",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2X 2S8",
  ;;         :shortText "H2X 2S8",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=8158419479446612102",
  ;;       :internationalPhoneNumber "+1 514-842-6927",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1310 Blvd. De Maisonneuve Ouest</span>, <span class=\"locality\">Montreal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3G 2P4</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Club Wanda's", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJm_V6x0EayUwRhugKmUqHOHE",
  ;;       :websiteUri "http://www.clubwandas.com/",
  ;;       :shortFormattedAddress
  ;;       "1310 Blvd. De Maisonneuve Ouest, Montreal",
  ;;       :formattedAddress
  ;;       "1310 Blvd. De Maisonneuve Ouest, Montreal, QC H3G 2P4, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1310",
  ;;         :shortText "1310",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulevard De Maisonneuve Ouest",
  ;;         :shortText "Blvd. De Maisonneuve Ouest",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Montreal",
  ;;         :shortText "Montreal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montreal",
  ;;         :shortText "Montreal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Quebec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3G 2P4",
  ;;         :shortText "H3G 2P4",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=1517291124569633172",
  ;;       :internationalPhoneNumber "+1 514-866-0495",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1258 Rue Stanley</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3B 2S7</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Chez Parée", :languageCode "fr"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJdRK8NkIayUwRlHHTJD2ADhU",
  ;;       :websiteUri "http://chezparee.ca/en/",
  ;;       :shortFormattedAddress "1258 Stanley St, Montreal",
  ;;       :formattedAddress
  ;;       "1258 Rue Stanley, Montréal, QC H3B 2S7, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1258",
  ;;         :shortText "1258",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Stanley",
  ;;         :shortText "Rue Stanley",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3B 2S7",
  ;;         :shortText "H3B 2S7",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=4999407789086411285",
  ;;       :internationalPhoneNumber "+1 833-687-2536",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1458 Rue Crescent</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3G 2B7</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName
  ;;       {:text "Muscle Men Male Strip Club Montreal & Male Strippers",
  ;;        :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ39LgnowbyUwRFQrRa-V2YUU",
  ;;       :websiteUri
  ;;       "https://musclemenmalerevue.com/male-strip-club-montreal-ca/",
  ;;       :shortFormattedAddress "1458 Crescent St, Montreal",
  ;;       :formattedAddress
  ;;       "1458 Rue Crescent, Montréal, QC H3G 2B7, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1458",
  ;;         :shortText "1458",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Crescent",
  ;;         :shortText "Rue Crescent",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3G 2B7",
  ;;         :shortText "H3G 2B7",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=746109439671734153",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1950 Boul. de Maisonneuve E</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2K 2C9</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "HunkOMania", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJSV8iJ1QbyUwRiQ8zE5W2Wgo",
  ;;       :websiteUri
  ;;       "https://hunkomanianyc.com/montreal-ca-male-strip-club.html",
  ;;       :shortFormattedAddress "1950 Boul. de Maisonneuve E, Montréal",
  ;;       :formattedAddress
  ;;       "1950 Boul. de Maisonneuve E, Montréal, QC H2K 2C9, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1950",
  ;;         :shortText "1950",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulevard de Maisonneuve Est",
  ;;         :shortText "Boul. de Maisonneuve E",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2K 2C9",
  ;;         :shortText "H2K 2C9",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=6371063978160679377",
  ;;       :internationalPhoneNumber "+1 866-872-4865",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1464 Rue Crescent</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3G 2B7</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName
  ;;       {:text "Hunk-O-Mania Male Strip Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ_bIfHBk7yUwR0emNy-6Qalg",
  ;;       :websiteUri
  ;;       "https://www.hunk-o-mania.com/show/montreal-male-strip-club.html",
  ;;       :shortFormattedAddress "1464 Crescent St, Montreal",
  ;;       :formattedAddress
  ;;       "1464 Rue Crescent, Montréal, QC H3G 2B7, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1464",
  ;;         :shortText "1464",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Crescent",
  ;;         :shortText "Rue Crescent",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3G 2B7",
  ;;         :shortText "H3G 2B7",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=16163736957810952373",
  ;;       :internationalPhoneNumber "+1 866-847-6859",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">6683 Rue Jean-Talon E #1100</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H1S 1N2</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName
  ;;       {:text "MontrealX Bachelor Party Strippers", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJF4C9iPcfyUwRtcgsvR8hUeA",
  ;;       :websiteUri "https://montrealx.com/",
  ;;       :shortFormattedAddress "6683 Rue Jean-Talon E #1100, Montreal",
  ;;       :formattedAddress
  ;;       "6683 Rue Jean-Talon E #1100, Montréal, QC H1S 1N2, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1100",
  ;;         :shortText "1100",
  ;;         :types ["subpremise"],
  ;;         :languageCode "en"}
  ;;        {:longText "6683",
  ;;         :shortText "6683",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Jean-Talon Est",
  ;;         :shortText "Rue Jean-Talon E",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Saint-Léonard",
  ;;         :shortText "Saint-Léonard",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H1S 1N2",
  ;;         :shortText "H1S 1N2",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=9790080524246081292",
  ;;       :internationalPhoneNumber "+1 514-484-8695",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">6820 Rue Saint-Jacques</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H4B 1V8</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Cabaret Les Amazones", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJrbFXo80QyUwRDF8o4l1a3Yc",
  ;;       :shortFormattedAddress "6820 Rue Saint-Jacques, Montréal",
  ;;       :formattedAddress
  ;;       "6820 Rue Saint-Jacques, Montréal, QC H4B 1V8, Canada",
  ;;       :addressComponents
  ;;       [{:longText "6820",
  ;;         :shortText "6820",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Saint-Jacques",
  ;;         :shortText "Rue Saint-Jacques",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Côte-des-Neiges - Notre-Dame-de-Grâce",
  ;;         :shortText "Côte-des-Neiges - Notre-Dame-de-Grâce",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H4B 1V8",
  ;;         :shortText "H4B 1V8",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=10700737311843432623",
  ;;       :internationalPhoneNumber "+1 514-861-5193",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1200 Rue Sainte-Catherine</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3B 1K1</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Bar Downtown", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJjXXmpscbyUwRrzgy4-OngJQ",
  ;;       :websiteUri "http://www.bardowntown.com/",
  ;;       :shortFormattedAddress "1200 Saint-Catherine St W, Montreal",
  ;;       :formattedAddress
  ;;       "1200 Rue Sainte-Catherine, Montréal, QC H3B 1K1, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1200",
  ;;         :shortText "1200",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Sainte-Catherine",
  ;;         :shortText "Rue Sainte-Catherine",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3B 1K1",
  ;;         :shortText "H3B 1K1",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=18391104778429759935",
  ;;       :internationalPhoneNumber "+1 514-526-3616",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1111 Rue Sainte-Catherine E</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2L 2G6</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Campus", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ-T3rC7IbyUwRv4FLEVtUOv8",
  ;;       :websiteUri "https://www.campusmtl.com/",
  ;;       :shortFormattedAddress "1111 St Catherine St E, Montreal",
  ;;       :formattedAddress
  ;;       "1111 Rue Sainte-Catherine E, Montréal, QC H2L 2G6, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1111",
  ;;         :shortText "1111",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Sainte-Catherine Est",
  ;;         :shortText "Rue Sainte-Catherine E",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2L 2G6",
  ;;         :shortText "H2L 2G6",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=4903756063350610692",
  ;;       :internationalPhoneNumber "+1 800-371-1224",
  ;;       :adrFormatAddress
  ;;       "2310 Ste Catherine West, <span class=\"street-address\">Forum</span>, <span class=\"locality\">Montréal, Québec</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3H 1M7</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Montreal New Years XS", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJae1AbW0ayUwRBFsujSikDUQ",
  ;;       :websiteUri "http://montrealnewyearsxs.com/",
  ;;       :shortFormattedAddress
  ;;       "2310 Ste Catherine West, Forum, Montréal, Québec",
  ;;       :formattedAddress
  ;;       "2310 Ste Catherine West, Forum, Montréal, Québec, QC H3H 1M7, Canada",
  ;;       :addressComponents
  ;;       [{:longText "Montréal, Québec",
  ;;         :shortText "Montréal, Québec",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montreal",
  ;;         :shortText "Montreal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Quebec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3H 1M7",
  ;;         :shortText "H3H 1M7",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=13397662817526083665",
  ;;       :internationalPhoneNumber "+1 514-971-9779",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">381 Rue Sainte-Catherine</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3B 5H1</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Sex Appeal", :languageCode "fr"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJFes_0EsbyUwRUfAZQ1kP7rk",
  ;;       :websiteUri "https://www.facebook.com/barsexappeal",
  ;;       :shortFormattedAddress "381 Saint-Catherine St W, Montreal",
  ;;       :formattedAddress
  ;;       "381 Rue Sainte-Catherine, Montréal, QC H3B 5H1, Canada",
  ;;       :addressComponents
  ;;       [{:longText "381",
  ;;         :shortText "381",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Sainte-Catherine",
  ;;         :shortText "Rue Sainte-Catherine",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3B 5H1",
  ;;         :shortText "H3B 5H1",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=8440055337325675106",
  ;;       :internationalPhoneNumber "+1 514-684-6280",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">2335 Boul Hymus</span>, <span class=\"locality\">Dorval</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H9P 1J8</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "La Source du Sexe", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJRY5SePs8yUwRYsLvqZwZIXU",
  ;;       :websiteUri "http://www.sourcedusexe.com/",
  ;;       :shortFormattedAddress "2335 Hymus Blvd, Dorval",
  ;;       :formattedAddress "2335 Boul Hymus, Dorval, QC H9P 1J8, Canada",
  ;;       :addressComponents
  ;;       [{:longText "2335",
  ;;         :shortText "2335",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulevard Hymus",
  ;;         :shortText "Boul Hymus",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Dorval",
  ;;         :shortText "Dorval",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H9P 1J8",
  ;;         :shortText "H9P 1J8",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=7774825077845718101",
  ;;       :internationalPhoneNumber "+1 514-842-1336",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1171 Rue Sainte-Catherine E</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2L 2G8</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Stock Bar", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJh2p6fq0byUwRVfTKKze65Ws",
  ;;       :websiteUri "http://www.stockbar.com/",
  ;;       :shortFormattedAddress "1171 St Catherine St E, Montreal",
  ;;       :formattedAddress
  ;;       "1171 Rue Sainte-Catherine E, Montréal, QC H2L 2G8, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1171",
  ;;         :shortText "1171",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Sainte-Catherine Est",
  ;;         :shortText "Rue Sainte-Catherine E",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2L 2G8",
  ;;         :shortText "H2L 2G8",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=1535385476296807684",
  ;;       :internationalPhoneNumber "+1 514-871-8065",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1230 Boul. Saint-Laurent</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2X 2S5</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Café Cléopatra", :languageCode "fr"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ50l7c04ayUwRBBX3s_TIThU",
  ;;       :websiteUri "http://www.cleopatramontreal.com/",
  ;;       :shortFormattedAddress "1230 St Laurent Blvd, Montreal",
  ;;       :formattedAddress
  ;;       "1230 Boul. Saint-Laurent, Montréal, QC H2X 2S5, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1230",
  ;;         :shortText "1230",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulevard Saint-Laurent",
  ;;         :shortText "Boul. Saint-Laurent",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2X 2S5",
  ;;         :shortText "H2X 2S5",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=10092317570821349184",
  ;;       :internationalPhoneNumber "+1 514-688-8573",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1055 Rue St Mathieu</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3H 2S5</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Danseurs Montreal", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJcaQLwk8byUwRQF89-VsdD4w",
  ;;       :websiteUri "http://www.danseursmontreal.com/",
  ;;       :shortFormattedAddress "1055 St Mathieu St, Montreal",
  ;;       :formattedAddress
  ;;       "1055 Rue St Mathieu, Montréal, QC H3H 2S5, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1055",
  ;;         :shortText "1055",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Saint Mathieu",
  ;;         :shortText "Rue St Mathieu",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3H 2S5",
  ;;         :shortText "H3H 2S5",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=15234793395707421776",
  ;;       :internationalPhoneNumber "+1 877-690-4919",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3414 Av du Parc</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2X 2H5</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Connected Montreal", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ7SYAhCsayUwRUKSbQeDbbNM",
  ;;       :websiteUri "http://www.connectedmontreal.com/",
  ;;       :shortFormattedAddress
  ;;       "EDDIFICE DU PARC, 3414 Park Ave, Montreal",
  ;;       :formattedAddress "3414 Av du Parc, Montréal, QC H2X 2H5, Canada",
  ;;       :addressComponents
  ;;       [{:longText "3414",
  ;;         :shortText "3414",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Avenue du Parc",
  ;;         :shortText "Av du Parc",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Le Plateau-Mont-Royal",
  ;;         :shortText "Le Plateau-Mont-Royal",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2X 2H5",
  ;;         :shortText "H2X 2H5",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}]}
  ;; => {:places
  ;;     [{:googleMapsUri
  ;;       "https://maps.google.com/?cid=16163736957810952373",
  ;;       :internationalPhoneNumber "+1 866-847-6859",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">6683 Rue Jean-Talon E #1100</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H1S 1N2</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName
  ;;       {:text "MontrealX Bachelor Party Strippers", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJF4C9iPcfyUwRtcgsvR8hUeA",
  ;;       :websiteUri "https://montrealx.com/",
  ;;       :shortFormattedAddress "6683 Rue Jean-Talon E #1100, Montreal",
  ;;       :formattedAddress
  ;;       "6683 Rue Jean-Talon E #1100, Montréal, QC H1S 1N2, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1100",
  ;;         :shortText "1100",
  ;;         :types ["subpremise"],
  ;;         :languageCode "en"}
  ;;        {:longText "6683",
  ;;         :shortText "6683",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Jean-Talon Est",
  ;;         :shortText "Rue Jean-Talon E",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Saint-Léonard",
  ;;         :shortText "Saint-Léonard",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H1S 1N2",
  ;;         :shortText "H1S 1N2",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=15731930692466354764",
  ;;       :internationalPhoneNumber "+1 514-934-3329",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3802 R. Notre Dame O</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H4C 1P9</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Montreal Strip Club", :languageCode "fr"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ3frKYp0QyUwRTKqFC50LU9o",
  ;;       :shortFormattedAddress "3802 Notre-Dame St W, Montreal",
  ;;       :formattedAddress
  ;;       "3802 R. Notre Dame O, Montréal, QC H4C 1P9, Canada",
  ;;       :addressComponents
  ;;       [{:longText "3802",
  ;;         :shortText "3802",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Notre-Dame Ouest",
  ;;         :shortText "R. Notre Dame O",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Le Sud-Ouest",
  ;;         :shortText "Le Sud-Ouest",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H4C 1P9",
  ;;         :shortText "H4C 1P9",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=16866874346928347528",
  ;;       :internationalPhoneNumber "+1 514-842-4892",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3580 Rue Saint-Dominique</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2X 2X4</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Kamasutra Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJA7XrhEoayUwRiBnQn9osE-o",
  ;;       :websiteUri "http://www.kamasutramtl.com/",
  ;;       :shortFormattedAddress "3580 Saint Dominique St., Montreal",
  ;;       :formattedAddress
  ;;       "3580 Rue Saint-Dominique, Montréal, QC H2X 2X4, Canada",
  ;;       :addressComponents
  ;;       [{:longText "3580",
  ;;         :shortText "3580",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Saint-Dominique",
  ;;         :shortText "Rue Saint-Dominique",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Le Plateau-Mont-Royal",
  ;;         :shortText "Le Plateau-Mont-Royal",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2X 2X4",
  ;;         :shortText "H2X 2X4",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=15456279697285596594",
  ;;       :internationalPhoneNumber "+1 514-286-1417",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1417 Boul. Saint-Laurent</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2X 2S8</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Cabaret Kingdom", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ-yeH1U0ayUwRskXKaX68f9Y",
  ;;       :websiteUri "https://www.kingdommontreal.com/",
  ;;       :shortFormattedAddress "1417 St Laurent Blvd, Montreal",
  ;;       :formattedAddress
  ;;       "1417 Boul. Saint-Laurent, Montréal, QC H2X 2S8, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1417",
  ;;         :shortText "1417",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulevard Saint-Laurent",
  ;;         :shortText "Boul. Saint-Laurent",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2X 2S8",
  ;;         :shortText "H2X 2S8",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=746109439671734153",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1950 Boul. de Maisonneuve E</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2K 2C9</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "HunkOMania", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJSV8iJ1QbyUwRiQ8zE5W2Wgo",
  ;;       :websiteUri
  ;;       "https://hunkomanianyc.com/montreal-ca-male-strip-club.html",
  ;;       :shortFormattedAddress "1950 Boul. de Maisonneuve E, Montréal",
  ;;       :formattedAddress
  ;;       "1950 Boul. de Maisonneuve E, Montréal, QC H2K 2C9, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1950",
  ;;         :shortText "1950",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulevard de Maisonneuve Est",
  ;;         :shortText "Boul. de Maisonneuve E",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2K 2C9",
  ;;         :shortText "H2K 2C9",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=8158419479446612102",
  ;;       :internationalPhoneNumber "+1 514-842-6927",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1310 Blvd. De Maisonneuve Ouest</span>, <span class=\"locality\">Montreal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3G 2P4</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Club Wanda's", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJm_V6x0EayUwRhugKmUqHOHE",
  ;;       :websiteUri "http://www.clubwandas.com/",
  ;;       :shortFormattedAddress
  ;;       "1310 Blvd. De Maisonneuve Ouest, Montreal",
  ;;       :formattedAddress
  ;;       "1310 Blvd. De Maisonneuve Ouest, Montreal, QC H3G 2P4, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1310",
  ;;         :shortText "1310",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulevard De Maisonneuve Ouest",
  ;;         :shortText "Blvd. De Maisonneuve Ouest",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Montreal",
  ;;         :shortText "Montreal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montreal",
  ;;         :shortText "Montreal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Quebec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3G 2P4",
  ;;         :shortText "H3G 2P4",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=1517291124569633172",
  ;;       :internationalPhoneNumber "+1 514-866-0495",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1258 Rue Stanley</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3B 2S7</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Chez Parée", :languageCode "fr"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJdRK8NkIayUwRlHHTJD2ADhU",
  ;;       :websiteUri "http://chezparee.ca/en/",
  ;;       :shortFormattedAddress "1258 Stanley St, Montreal",
  ;;       :formattedAddress
  ;;       "1258 Rue Stanley, Montréal, QC H3B 2S7, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1258",
  ;;         :shortText "1258",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Stanley",
  ;;         :shortText "Rue Stanley",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3B 2S7",
  ;;         :shortText "H3B 2S7",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=9790080524246081292",
  ;;       :internationalPhoneNumber "+1 514-484-8695",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">6820 Rue Saint-Jacques</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H4B 1V8</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Cabaret Les Amazones", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJrbFXo80QyUwRDF8o4l1a3Yc",
  ;;       :shortFormattedAddress "6820 Rue Saint-Jacques, Montréal",
  ;;       :formattedAddress
  ;;       "6820 Rue Saint-Jacques, Montréal, QC H4B 1V8, Canada",
  ;;       :addressComponents
  ;;       [{:longText "6820",
  ;;         :shortText "6820",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Saint-Jacques",
  ;;         :shortText "Rue Saint-Jacques",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Côte-des-Neiges - Notre-Dame-de-Grâce",
  ;;         :shortText "Côte-des-Neiges - Notre-Dame-de-Grâce",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H4B 1V8",
  ;;         :shortText "H4B 1V8",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=6371063978160679377",
  ;;       :internationalPhoneNumber "+1 866-872-4865",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1464 Rue Crescent</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3G 2B7</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName
  ;;       {:text "Hunk-O-Mania Male Strip Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ_bIfHBk7yUwR0emNy-6Qalg",
  ;;       :websiteUri
  ;;       "https://www.hunk-o-mania.com/show/montreal-male-strip-club.html",
  ;;       :shortFormattedAddress "1464 Crescent St, Montreal",
  ;;       :formattedAddress
  ;;       "1464 Rue Crescent, Montréal, QC H3G 2B7, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1464",
  ;;         :shortText "1464",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Crescent",
  ;;         :shortText "Rue Crescent",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3G 2B7",
  ;;         :shortText "H3G 2B7",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=10700737311843432623",
  ;;       :internationalPhoneNumber "+1 514-861-5193",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1200 Rue Sainte-Catherine</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3B 1K1</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Bar Downtown", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJjXXmpscbyUwRrzgy4-OngJQ",
  ;;       :websiteUri "http://www.bardowntown.com/",
  ;;       :shortFormattedAddress "1200 Saint-Catherine St W, Montreal",
  ;;       :formattedAddress
  ;;       "1200 Rue Sainte-Catherine, Montréal, QC H3B 1K1, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1200",
  ;;         :shortText "1200",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Sainte-Catherine",
  ;;         :shortText "Rue Sainte-Catherine",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3B 1K1",
  ;;         :shortText "H3B 1K1",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=12120999070332251898",
  ;;       :internationalPhoneNumber "+1 613-676-2373",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">200 Boul. René-Lévesque O</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2Z 1X4</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "GrudgeX", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ82b0NQAbyUwR-m7-RW9wNqg",
  ;;       :shortFormattedAddress "200 René-Lévesque Blvd W, Montreal",
  ;;       :formattedAddress
  ;;       "200 Boul. René-Lévesque O, Montréal, QC H2Z 1X4, Canada",
  ;;       :addressComponents
  ;;       [{:longText "200",
  ;;         :shortText "200",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulevard René-Lévesque Ouest",
  ;;         :shortText "Boul. René-Lévesque O",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2Z 1X4",
  ;;         :shortText "H2Z 1X4",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=18391104778429759935",
  ;;       :internationalPhoneNumber "+1 514-526-3616",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1111 Rue Sainte-Catherine E</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2L 2G6</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Campus", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ-T3rC7IbyUwRv4FLEVtUOv8",
  ;;       :websiteUri "https://www.campusmtl.com/",
  ;;       :shortFormattedAddress "1111 St Catherine St E, Montreal",
  ;;       :formattedAddress
  ;;       "1111 Rue Sainte-Catherine E, Montréal, QC H2L 2G6, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1111",
  ;;         :shortText "1111",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Sainte-Catherine Est",
  ;;         :shortText "Rue Sainte-Catherine E",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2L 2G6",
  ;;         :shortText "H2L 2G6",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=4999407789086411285",
  ;;       :internationalPhoneNumber "+1 833-687-2536",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1458 Rue Crescent</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3G 2B7</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName
  ;;       {:text "Muscle Men Male Strip Club Montreal & Male Strippers",
  ;;        :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ39LgnowbyUwRFQrRa-V2YUU",
  ;;       :websiteUri
  ;;       "https://musclemenmalerevue.com/male-strip-club-montreal-ca/",
  ;;       :shortFormattedAddress "1458 Crescent St, Montreal",
  ;;       :formattedAddress
  ;;       "1458 Rue Crescent, Montréal, QC H3G 2B7, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1458",
  ;;         :shortText "1458",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Crescent",
  ;;         :shortText "Rue Crescent",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3G 2B7",
  ;;         :shortText "H3G 2B7",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=4903756063350610692",
  ;;       :internationalPhoneNumber "+1 800-371-1224",
  ;;       :adrFormatAddress
  ;;       "2310 Ste Catherine West, <span class=\"street-address\">Forum</span>, <span class=\"locality\">Montréal, Québec</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3H 1M7</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Montreal New Years XS", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJae1AbW0ayUwRBFsujSikDUQ",
  ;;       :websiteUri "http://montrealnewyearsxs.com/",
  ;;       :shortFormattedAddress
  ;;       "2310 Ste Catherine West, Forum, Montréal, Québec",
  ;;       :formattedAddress
  ;;       "2310 Ste Catherine West, Forum, Montréal, Québec, QC H3H 1M7, Canada",
  ;;       :addressComponents
  ;;       [{:longText "Montréal, Québec",
  ;;         :shortText "Montréal, Québec",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montreal",
  ;;         :shortText "Montreal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Quebec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3H 1M7",
  ;;         :shortText "H3H 1M7",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=7774825077845718101",
  ;;       :internationalPhoneNumber "+1 514-842-1336",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1171 Rue Sainte-Catherine E</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2L 2G8</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Stock Bar", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJh2p6fq0byUwRVfTKKze65Ws",
  ;;       :websiteUri "http://www.stockbar.com/",
  ;;       :shortFormattedAddress "1171 St Catherine St E, Montreal",
  ;;       :formattedAddress
  ;;       "1171 Rue Sainte-Catherine E, Montréal, QC H2L 2G8, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1171",
  ;;         :shortText "1171",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Sainte-Catherine Est",
  ;;         :shortText "Rue Sainte-Catherine E",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2L 2G8",
  ;;         :shortText "H2L 2G8",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=8440055337325675106",
  ;;       :internationalPhoneNumber "+1 514-684-6280",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">2335 Boul Hymus</span>, <span class=\"locality\">Dorval</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H9P 1J8</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "La Source du Sexe", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJRY5SePs8yUwRYsLvqZwZIXU",
  ;;       :websiteUri "http://www.sourcedusexe.com/",
  ;;       :shortFormattedAddress "2335 Hymus Blvd, Dorval",
  ;;       :formattedAddress "2335 Boul Hymus, Dorval, QC H9P 1J8, Canada",
  ;;       :addressComponents
  ;;       [{:longText "2335",
  ;;         :shortText "2335",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulevard Hymus",
  ;;         :shortText "Boul Hymus",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Dorval",
  ;;         :shortText "Dorval",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H9P 1J8",
  ;;         :shortText "H9P 1J8",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=7161483771904698566",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1478 Rue Crescent</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3G 1S8</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName
  ;;       {:text "Naked Male Butlers Montreal", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ74ST5EEbyUwRxqgaIYyzYmM",
  ;;       :websiteUri
  ;;       "https://www.nakedmalebutlers.com/naked-male-butlers-montreal-ca.html",
  ;;       :shortFormattedAddress "1478 Crescent St, Montreal",
  ;;       :formattedAddress
  ;;       "1478 Rue Crescent, Montréal, QC H3G 1S8, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1478",
  ;;         :shortText "1478",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Crescent",
  ;;         :shortText "Rue Crescent",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3G 1S8",
  ;;         :shortText "H3G 1S8",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=14847229103041453671",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1681 Rue Sainte-Catherine E</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2L 2J5</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "bar Diamant Rouge", :languageCode "fr"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJkfB_V9EbyUwRZ56giTT0C84",
  ;;       :websiteUri "https://expose-strip-bar.business.site/",
  ;;       :shortFormattedAddress "1681 St Catherine St E, Montreal",
  ;;       :formattedAddress
  ;;       "1681 Rue Sainte-Catherine E, Montréal, QC H2L 2J5, Canada",
  ;;       :addressComponents
  ;;       [{:longText "1681",
  ;;         :shortText "1681",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Sainte-Catherine Est",
  ;;         :shortText "Rue Sainte-Catherine E",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2L 2J5",
  ;;         :shortText "H2L 2J5",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=13397662817526083665",
  ;;       :internationalPhoneNumber "+1 514-971-9779",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">381 Rue Sainte-Catherine</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H3B 5H1</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Sex Appeal", :languageCode "fr"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJFes_0EsbyUwRUfAZQ1kP7rk",
  ;;       :websiteUri "https://www.facebook.com/barsexappeal",
  ;;       :shortFormattedAddress "381 Saint-Catherine St W, Montreal",
  ;;       :formattedAddress
  ;;       "381 Rue Sainte-Catherine, Montréal, QC H3B 5H1, Canada",
  ;;       :addressComponents
  ;;       [{:longText "381",
  ;;         :shortText "381",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Sainte-Catherine",
  ;;         :shortText "Rue Sainte-Catherine",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H3B 5H1",
  ;;         :shortText "H3B 5H1",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=10531253217862113974",
  ;;       :internationalPhoneNumber "+1 514-526-9166",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">2683 Rue Ontario E</span>, <span class=\"locality\">Montréal</span>, <span class=\"region\">QC</span> <span class=\"postal-code\">H2K 1X1</span>, <span class=\"country-name\">Canada</span>",
  ;;       :displayName {:text "Bar Mania", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJbZjaXZYbyUwRtlpcMgGHJpI",
  ;;       :websiteUri "https://www.facebook.com/BarManiaMontreal/",
  ;;       :shortFormattedAddress "2683 Ontario St E, Montreal",
  ;;       :formattedAddress
  ;;       "2683 Rue Ontario E, Montréal, QC H2K 1X1, Canada",
  ;;       :addressComponents
  ;;       [{:longText "2683",
  ;;         :shortText "2683",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Rue Ontario East",
  ;;         :shortText "Rue Ontario E",
  ;;         :types ["route"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Ville-Marie",
  ;;         :shortText "Ville-Marie",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_3" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Montréal",
  ;;         :shortText "Montréal",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Québec",
  ;;         :shortText "QC",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "fr"}
  ;;        {:longText "Canada",
  ;;         :shortText "CA",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "H2K 1X1",
  ;;         :shortText "H2K 1X1",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}]}

  
  ;; => {:places
  ;;     [{:googleMapsUri "https://maps.google.com/?cid=692564365649707855",
  ;;       :internationalPhoneNumber "+1 702-366-1141",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1514 Western Ave</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89102</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Little Darlings Las Vegas", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJlRqurI3DyIARTwOlBp97nAk",
  ;;       :websiteUri "http://www.littledarlingsvegas.com/",
  ;;       :shortFormattedAddress "1514 Western Ave, Las Vegas",
  ;;       :formattedAddress "1514 Western Ave, Las Vegas, NV 89102, USA",
  ;;       :addressComponents
  ;;       [{:longText "1514",
  ;;         :shortText "1514",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Western Avenue",
  ;;         :shortText "Western Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Downtown Las Vegas",
  ;;         :shortText "Downtown Las Vegas",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89102",
  ;;         :shortText "89102",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=6660283108148430345",
  ;;       :internationalPhoneNumber "+1 702-869-0003",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3025 S Sammy Davis Jr Dr</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89109</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Sapphire Las Vegas Gentlemen's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJj8bp9xXEyIARCca4_DIUblw",
  ;;       :websiteUri "http://www.sapphirelasvegas.com/",
  ;;       :shortFormattedAddress "3025 S Sammy Davis Jr Dr, Las Vegas",
  ;;       :formattedAddress
  ;;       "3025 S Sammy Davis Jr Dr, Las Vegas, NV 89109, USA",
  ;;       :addressComponents
  ;;       [{:longText "3025",
  ;;         :shortText "3025",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "South Sammy Davis Junior Drive",
  ;;         :shortText "S Sammy Davis Jr Dr",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89109",
  ;;         :shortText "89109",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=7680593528514511672",
  ;;       :internationalPhoneNumber "+1 702-916-1499",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">2995 S Highland Dr</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89109</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Scores Las Vegas Gentleman's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJn6Izpg_EyIAROINF4x7zlmo",
  ;;       :websiteUri "https://scoreslv.com/",
  ;;       :shortFormattedAddress "2995 S Highland Dr, Las Vegas",
  ;;       :formattedAddress "2995 S Highland Dr, Las Vegas, NV 89109, USA",
  ;;       :addressComponents
  ;;       [{:longText "2995",
  ;;         :shortText "2995",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "South Highland Drive",
  ;;         :shortText "S Highland Dr",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Charleston",
  ;;         :shortText "Charleston",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89109",
  ;;         :shortText "89109",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=18337898647961474960",
  ;;       :internationalPhoneNumber "+1 702-894-4167",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3247 S Sammy Davis Jr Dr</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89109-1140</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Deja Vu Showgirls Las Vegas", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJf2WtVRDEyIARkBfcdqlNff4",
  ;;       :websiteUri "http://dejavuvegas.com/",
  ;;       :shortFormattedAddress "3247 S Sammy Davis Jr Dr, Las Vegas",
  ;;       :formattedAddress
  ;;       "3247 S Sammy Davis Jr Dr, Las Vegas, NV 89109, USA",
  ;;       :addressComponents
  ;;       [{:longText "3247",
  ;;         :shortText "3247",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "South Sammy Davis Junior Drive",
  ;;         :shortText "S Sammy Davis Jr Dr",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89109",
  ;;         :shortText "89109",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "1140",
  ;;         :shortText "1140",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=4552568758174268777",
  ;;       :internationalPhoneNumber "+1 702-890-3731",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3750 S Valley View Blvd</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89103-2928</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Honeys Gentlemen’s Club Las Vegas", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ7a6ZYaDGyIARab2EPir5LT8",
  ;;       :websiteUri "http://honeyslasvegas.com/",
  ;;       :shortFormattedAddress "3750 S Valley View Blvd, Las Vegas",
  ;;       :formattedAddress
  ;;       "3750 S Valley View Blvd, Las Vegas, NV 89103, USA",
  ;;       :addressComponents
  ;;       [{:longText "3750",
  ;;         :shortText "3750",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "South Valley View Boulevard",
  ;;         :shortText "S Valley View Blvd",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89103",
  ;;         :shortText "89103",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "2928",
  ;;         :shortText "2928",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=8510802526997494598",
  ;;       :internationalPhoneNumber "+1 702-796-3600",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3340 S Highland Dr</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89109-3427</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Spearmint Rhino Gentlemen's Club Las Vegas",
  ;;        :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJV3syXRrEyIARRrsuX85xHHY",
  ;;       :websiteUri "http://www.spearmintrhinolv.com/",
  ;;       :shortFormattedAddress "3340 S Highland Dr, Las Vegas",
  ;;       :formattedAddress "3340 S Highland Dr, Las Vegas, NV 89109, USA",
  ;;       :addressComponents
  ;;       [{:longText "3340",
  ;;         :shortText "3340",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "South Highland Drive",
  ;;         :shortText "S Highland Dr",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Charleston",
  ;;         :shortText "Charleston",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89109",
  ;;         :shortText "89109",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "3427",
  ;;         :shortText "3427",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=10159447274712329497",
  ;;       :internationalPhoneNumber "+1 702-710-7419",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">2217 Paradise Rd suite b</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89104-2514</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Strip Club Plug LV", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJo0LE7R7FyIARGe2JI3ib_Yw",
  ;;       :websiteUri "http://stripclubpluglv.com/book-now/",
  ;;       :shortFormattedAddress "2217 Paradise Rd suite b, Las Vegas",
  ;;       :formattedAddress
  ;;       "2217 Paradise Rd suite b, Las Vegas, NV 89104, USA",
  ;;       :addressComponents
  ;;       [{:longText "suite b",
  ;;         :shortText "suite b",
  ;;         :types ["subpremise"],
  ;;         :languageCode "en"}
  ;;        {:longText "2217",
  ;;         :shortText "2217",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Paradise Road",
  ;;         :shortText "Paradise Rd",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Downtown South",
  ;;         :shortText "Downtown South",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89104",
  ;;         :shortText "89104",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "2514",
  ;;         :shortText "2514",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=3977502218638235170",
  ;;       :internationalPhoneNumber "+1 702-642-2984",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1848 Las Vegas Blvd N</span>, <span class=\"locality\">North Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89030</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Palomino Las Vegas Gentlemen's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJYdREcYjDyIARIpZCszHtMjc",
  ;;       :websiteUri
  ;;       "https://palominolv.com/?utm_source=google&utm_medium=organic&utm_campaign=GMB-Las-Vegas",
  ;;       :shortFormattedAddress "1848 Las Vegas Blvd N, North Las Vegas",
  ;;       :formattedAddress
  ;;       "1848 Las Vegas Blvd N, North Las Vegas, NV 89030, USA",
  ;;       :addressComponents
  ;;       [{:longText "1848",
  ;;         :shortText "1848",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Las Vegas Boulevard North",
  ;;         :shortText "Las Vegas Blvd N",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "North Las Vegas",
  ;;         :shortText "North Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89030",
  ;;         :shortText "89030",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=1325662075692123253",
  ;;       :internationalPhoneNumber "+1 702-384-0074",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">2112 Western Ave</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89102</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "The Library Gentlemen's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJazRM1_TDyIARdQTWWaKyZRI",
  ;;       :websiteUri "https://thelibrarygc.com/",
  ;;       :shortFormattedAddress "2112 Western Ave, Las Vegas",
  ;;       :formattedAddress "2112 Western Ave, Las Vegas, NV 89102, USA",
  ;;       :addressComponents
  ;;       [{:longText "2112",
  ;;         :shortText "2112",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Western Avenue",
  ;;         :shortText "Western Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Downtown Las Vegas",
  ;;         :shortText "Downtown Las Vegas",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89102",
  ;;         :shortText "89102",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=6380471881171378820",
  ;;       :internationalPhoneNumber "+1 702-257-3030",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">2801 Westwood Dr</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89109</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Treasures Gentlemen's Club & Steakhouse",
  ;;        :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJE6g-egnEyIARhKKW6F79i1g",
  ;;       :websiteUri
  ;;       "https://treasureslasvegas.com/?utm_source=Google&utm_medium=GBP&utm_campaign=Local_seo&utm_id=Local_SEO",
  ;;       :shortFormattedAddress "2801 Westwood Dr, Las Vegas",
  ;;       :formattedAddress "2801 Westwood Dr, Las Vegas, NV 89109, USA",
  ;;       :addressComponents
  ;;       [{:longText "2801",
  ;;         :shortText "2801",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Westwood Drive",
  ;;         :shortText "Westwood Dr",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Charleston",
  ;;         :shortText "Charleston",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89109",
  ;;         :shortText "89109",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=2068388877936120274",
  ;;       :internationalPhoneNumber "+1 702-473-9977",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1531 Las Vegas Blvd S</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89104</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Peppermint Hippo", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJV1rlcgfDyIAR0lHAxrlktBw",
  ;;       :websiteUri
  ;;       "https://www.thepepperminthippo.com/locations/las-vegas-nv/",
  ;;       :shortFormattedAddress "1531 Las Vegas Blvd S, Las Vegas",
  ;;       :formattedAddress
  ;;       "1531 Las Vegas Blvd S, Las Vegas, NV 89104, USA",
  ;;       :addressComponents
  ;;       [{:longText "1531",
  ;;         :shortText "1531",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Las Vegas Boulevard South",
  ;;         :shortText "Las Vegas Blvd S",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Downtown South",
  ;;         :shortText "Downtown South",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89104",
  ;;         :shortText "89104",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=16129699157273728530",
  ;;       :internationalPhoneNumber "+1 702-755-1766",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3585 S Highland Dr #46</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89103</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Strip Club Concierge Las Vegas", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJiar7EpzDyIAREtY3u-wz2N8",
  ;;       :websiteUri "https://www.stripclubconcierge.com/",
  ;;       :shortFormattedAddress "3585 S Highland Dr #46, Las Vegas",
  ;;       :formattedAddress
  ;;       "3585 S Highland Dr #46, Las Vegas, NV 89103, USA",
  ;;       :addressComponents
  ;;       [{:longText "#46",
  ;;         :shortText "#46",
  ;;         :types ["subpremise"],
  ;;         :languageCode "en"}
  ;;        {:longText "3585",
  ;;         :shortText "3585",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "South Highland Drive",
  ;;         :shortText "S Highland Dr",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89103",
  ;;         :shortText "89103",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=13512241688797079412",
  ;;       :internationalPhoneNumber "+1 702-463-4127",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3177 S Highland Dr</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89109-1010</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Diamond Cabaret", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJpWNdAA_FyIARdGf5mzkghbs",
  ;;       :websiteUri "https://diamondcabaretvegas.com/",
  ;;       :shortFormattedAddress "3177 S Highland Dr, Las Vegas",
  ;;       :formattedAddress "3177 S Highland Dr, Las Vegas, NV 89109, USA",
  ;;       :addressComponents
  ;;       [{:longText "3177",
  ;;         :shortText "3177",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "South Highland Drive",
  ;;         :shortText "S Highland Dr",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Charleston",
  ;;         :shortText "Charleston",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89109",
  ;;         :shortText "89109",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "1010",
  ;;         :shortText "1010",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=9296126385043339453",
  ;;       :internationalPhoneNumber "+1 702-306-5083",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">300 S 4th St Ste 661B</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89101-6004</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Strip Club Concierge Las Vegas Downtown",
  ;;        :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJLQgJP7fDyIARvfikt7F5AoE",
  ;;       :websiteUri
  ;;       "https://www.stripclubconcierge.com/downtown-las-vegas/",
  ;;       :shortFormattedAddress "300 S 4th St Ste 661B, Las Vegas",
  ;;       :formattedAddress
  ;;       "300 S 4th St Ste 661B, Las Vegas, NV 89101, USA",
  ;;       :addressComponents
  ;;       [{:longText "Ste 661B",
  ;;         :shortText "Ste 661B",
  ;;         :types ["subpremise"],
  ;;         :languageCode "en"}
  ;;        {:longText "300",
  ;;         :shortText "300",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "South 4th Street",
  ;;         :shortText "S 4th St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Downtown Las Vegas",
  ;;         :shortText "Downtown Las Vegas",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89101",
  ;;         :shortText "89101",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "6004",
  ;;         :shortText "6004",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=2252298022203417457",
  ;;       :internationalPhoneNumber "+1 725-279-8384",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">211 N 8th St unit 358</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89101-4202</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "STRIP CLUB PLUG", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJGdDCq_nDyIARceO_NR7FQR8",
  ;;       :websiteUri "https://stripclubplug.com/",
  ;;       :shortFormattedAddress "211 N 8th St unit 358, Las Vegas",
  ;;       :formattedAddress
  ;;       "211 N 8th St unit 358, Las Vegas, NV 89101, USA",
  ;;       :addressComponents
  ;;       [{:longText "unit 358",
  ;;         :shortText "unit 358",
  ;;         :types ["subpremise"],
  ;;         :languageCode "en"}
  ;;        {:longText "211",
  ;;         :shortText "211",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "North 8th Street",
  ;;         :shortText "N 8th St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Downtown Las Vegas",
  ;;         :shortText "Downtown Las Vegas",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89101",
  ;;         :shortText "89101",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "4202",
  ;;         :shortText "4202",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=7963690934400070405",
  ;;       :internationalPhoneNumber "+1 702-876-1550",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">4120 Spring Mountain Rd</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89102-8702</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Play It Again Sam's", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ17-s36XGyIARBTPqHba2hG4",
  ;;       :websiteUri "http://www.playitagainsams.com/",
  ;;       :shortFormattedAddress "4120 Spring Mountain Rd, Las Vegas",
  ;;       :formattedAddress
  ;;       "4120 Spring Mountain Rd, Las Vegas, NV 89102, USA",
  ;;       :addressComponents
  ;;       [{:longText "4120",
  ;;         :shortText "4120",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Spring Mountain Road",
  ;;         :shortText "Spring Mountain Rd",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89102",
  ;;         :shortText "89102",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "8702",
  ;;         :shortText "8702",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=2958174039537459897",
  ;;       :internationalPhoneNumber "+1 702-734-7990",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">4416 Paradise Rd</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89169</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Centerfolds Cabaret Las Vegas", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ7wd081LEyIARuaJpqp2LDSk",
  ;;       :websiteUri "https://centerfoldslasvegas.com/",
  ;;       :shortFormattedAddress "4416 Paradise Rd, Las Vegas",
  ;;       :formattedAddress "4416 Paradise Rd, Las Vegas, NV 89169, USA",
  ;;       :addressComponents
  ;;       [{:longText "4416",
  ;;         :shortText "4416",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Paradise Road",
  ;;         :shortText "Paradise Rd",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "University District",
  ;;         :shortText "University District",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89169",
  ;;         :shortText "89169",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=12632668893754145789",
  ;;       :internationalPhoneNumber "+1 702-256-7894",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1818 Las Vegas Blvd N</span>, <span class=\"locality\">North Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89030</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Chicas Bonitas", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ4SBNjgLDyIAR_btXmW1BUK8",
  ;;       :websiteUri
  ;;       "https://chicasbonitaslocations.vdigitalservices.com/?utm_source=gmb&utm_medium=referral",
  ;;       :shortFormattedAddress "1818 Las Vegas Blvd N, North Las Vegas",
  ;;       :formattedAddress
  ;;       "1818 Las Vegas Blvd N, North Las Vegas, NV 89030, USA",
  ;;       :addressComponents
  ;;       [{:longText "1818",
  ;;         :shortText "1818",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Las Vegas Boulevard North",
  ;;         :shortText "Las Vegas Blvd N",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "North Las Vegas",
  ;;         :shortText "North Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89030",
  ;;         :shortText "89030",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=950822431595505821",
  ;;       :internationalPhoneNumber "+1 702-628-2060",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3960 Howard Hughes Pkwy Suite 563-02</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89169</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Vegas Best Strip Club Service", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJlbxGv7DFyIARnSi5v_b_MQ0",
  ;;       :websiteUri "https://vegasbeststripclub.com/",
  ;;       :shortFormattedAddress
  ;;       "3960 Howard Hughes Pkwy Suite 563-02, Las Vegas",
  ;;       :formattedAddress
  ;;       "3960 Howard Hughes Pkwy Suite 563-02, Las Vegas, NV 89169, USA",
  ;;       :addressComponents
  ;;       [{:longText "Suite 563-02",
  ;;         :shortText "Suite 563-02",
  ;;         :types ["subpremise"],
  ;;         :languageCode "en"}
  ;;        {:longText "3960",
  ;;         :shortText "3960",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Howard Hughes Parkway",
  ;;         :shortText "Howard Hughes Pkwy",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89169",
  ;;         :shortText "89169",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=8911267425786913074",
  ;;       :internationalPhoneNumber "+1 702-780-5939",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">3785 Boulder Hwy</span>, <span class=\"locality\">Las Vegas</span>, <span class=\"region\">NV</span> <span class=\"postal-code\">89121</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "LVLV Gentlemen's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJu5gCb1fbyIARMuGJhIIuq3s",
  ;;       :websiteUri "http://thelvlv.com/",
  ;;       :shortFormattedAddress "3785 Boulder Hwy, Las Vegas",
  ;;       :formattedAddress "3785 Boulder Hwy, Las Vegas, NV 89121, USA",
  ;;       :addressComponents
  ;;       [{:longText "3785",
  ;;         :shortText "3785",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Boulder Highway",
  ;;         :shortText "Boulder Hwy",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Las Vegas",
  ;;         :shortText "Las Vegas",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Clark County",
  ;;         :shortText "Clark County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Nevada",
  ;;         :shortText "NV",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "89121",
  ;;         :shortText "89121",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}]}
  ;; => {:places
  ;;     [{:googleMapsUri "https://maps.google.com/?cid=9217281361120604343",
  ;;       :internationalPhoneNumber "+1 718-706-9600",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">51-07 27th St</span>, <span class=\"locality\">Queens</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">11101</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Sugardaddy's Gentlemen's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJIx3TXzNZwokRtzBsDJBc6n8",
  ;;       :websiteUri "https://sugardaddysnyc.com/",
  ;;       :shortFormattedAddress "51-07 27th St, Queens",
  ;;       :formattedAddress "51-07 27th St, Queens, NY 11101, USA",
  ;;       :addressComponents
  ;;       [{:longText "51-07",
  ;;         :shortText "51-07",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "27th Street",
  ;;         :shortText "27th St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Long Island City",
  ;;         :shortText "LIC",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Queens",
  ;;         :shortText "Queens",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Queens County",
  ;;         :shortText "Queens County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "11101",
  ;;         :shortText "11101",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=8916803165869882543",
  ;;       :internationalPhoneNumber "+1 212-421-3600",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1674 Broadway</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10019</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Sapphire Times Square NYC Gentlemen's Club",
  ;;        :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJS63zgLFZwokRr7zAcTzZvns",
  ;;       :websiteUri "http://www.sapphiretimessquare.com/",
  ;;       :shortFormattedAddress "1674 Broadway, New York",
  ;;       :formattedAddress "1674 Broadway, New York, NY 10019, USA",
  ;;       :addressComponents
  ;;       [{:longText "1674",
  ;;         :shortText "1674",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Broadway",
  ;;         :shortText "Broadway",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10019",
  ;;         :shortText "10019",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=4025508835715950589",
  ;;       :internationalPhoneNumber "+1 212-372-0850",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">50 W 33rd St</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10001-3302</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Rick's Cabaret New York", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ2VhDH6lZwokR_acLQfV63Tc",
  ;;       :websiteUri "https://ricksnewyork.com/",
  ;;       :shortFormattedAddress "50 W 33rd St, New York",
  ;;       :formattedAddress "50 W 33rd St, New York, NY 10001, USA",
  ;;       :addressComponents
  ;;       [{:longText "50",
  ;;         :shortText "50",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "West 33rd Street",
  ;;         :shortText "W 33rd St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10001",
  ;;         :shortText "10001",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "3302",
  ;;         :shortText "3302",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=13022894467705872501",
  ;;       :internationalPhoneNumber "+1 212-247-2460",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">641 W 51st St</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10019-5008</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Hustler Club NYC | Best New York Strip Club",
  ;;        :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ35cNMJBZwokRdSACJ4WdurQ",
  ;;       :websiteUri "https://hustlerny.com/",
  ;;       :shortFormattedAddress "641 W 51st St, New York",
  ;;       :formattedAddress "641 W 51st St, New York, NY 10019, USA",
  ;;       :addressComponents
  ;;       [{:longText "641",
  ;;         :shortText "641",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "West 51st Street",
  ;;         :shortText "W 51st St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10019",
  ;;         :shortText "10019",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "5008",
  ;;         :shortText "5008",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=11328762327462399918",
  ;;       :internationalPhoneNumber "+1 212-922-0995",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">622 W 47th St</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10036-1907</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Spearmint Rhino Gentlemen's Club New York City",
  ;;        :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJi32IRNBZwokRrk85gVDZN50",
  ;;       :websiteUri "https://www.spearmintrhinonyc.com/",
  ;;       :shortFormattedAddress "622 W 47th St, New York",
  ;;       :formattedAddress "622 W 47th St, New York, NY 10036, USA",
  ;;       :addressComponents
  ;;       [{:longText "622",
  ;;         :shortText "622",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "West 47th Street",
  ;;         :shortText "W 47th St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10036",
  ;;         :shortText "10036",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "1907",
  ;;         :shortText "1907",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=12355035344898554437",
  ;;       :internationalPhoneNumber "+1 212-764-6969",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">725 7th Ave</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10019</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Platinum Dolls Gentlemen's Club & Sports Bar",
  ;;        :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJUUUMPrlZwokRRfoSuC_ndas",
  ;;       :websiteUri "http://www.platinumdollsnyc.com/",
  ;;       :shortFormattedAddress "725 7th Ave, New York",
  ;;       :formattedAddress "725 7th Ave, New York, NY 10019, USA",
  ;;       :addressComponents
  ;;       [{:longText "725",
  ;;         :shortText "725",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "7th Avenue",
  ;;         :shortText "7th Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10019",
  ;;         :shortText "10019",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=16913634599333076917",
  ;;       :internationalPhoneNumber "+1 212-315-5107",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">320 W 45th St</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10036</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Flashdancers NYC", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJgVaT1VdYwokRtR8BSg5Nueo",
  ;;       :websiteUri "http://www.flashdancersnyc.com/",
  ;;       :shortFormattedAddress "320 W 45th St, New York",
  ;;       :formattedAddress "320 W 45th St, New York, NY 10036, USA",
  ;;       :addressComponents
  ;;       [{:longText "320",
  ;;         :shortText "320",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "West 45th Street",
  ;;         :shortText "W 45th St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10036",
  ;;         :shortText "10036",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=17555939367171193569",
  ;;       :internationalPhoneNumber "+1 917-397-5953",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">552 W 38th St</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10018</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "HQ KONY Gentlemen's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ6_2TsExYwokR4fqIztY5o_M",
  ;;       :websiteUri "http://hqkony.com/",
  ;;       :shortFormattedAddress "552 W 38th St, New York",
  ;;       :formattedAddress "552 W 38th St, New York, NY 10018, USA",
  ;;       :addressComponents
  ;;       [{:longText "552",
  ;;         :shortText "552",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "West 38th Street",
  ;;         :shortText "W 38th St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10018",
  ;;         :shortText "10018",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=4319779163006184804",
  ;;       :internationalPhoneNumber "+1 718-893-4466",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">673 Hunts Point Ave</span>, <span class=\"locality\">Bronx</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10474</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Diamond Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJk8XlvmX1wokRZC0e90Lw8js",
  ;;       :websiteUri "http://diamondclubbx.com/",
  ;;       :shortFormattedAddress "673 Hunts Point Ave, Bronx",
  ;;       :formattedAddress "673 Hunts Point Ave, Bronx, NY 10474, USA",
  ;;       :addressComponents
  ;;       [{:longText "673",
  ;;         :shortText "673",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Hunts Point Avenue",
  ;;         :shortText "Hunts Point Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Hunts Point",
  ;;         :shortText "Hunts Point",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "The Bronx",
  ;;         :shortText "Bronx",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Bronx County",
  ;;         :shortText "Bronx County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10474",
  ;;         :shortText "10474",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=5251832686195945841",
  ;;       :internationalPhoneNumber "+1 212-421-3600",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">333 E 60th St</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10022-1505</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Sapphire New York", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ15X9N-ZYwokRcbF-sABC4kg",
  ;;       :websiteUri "http://www.nysapphire.com/",
  ;;       :shortFormattedAddress "333 E 60th St, New York",
  ;;       :formattedAddress "333 E 60th St, New York, NY 10022, USA",
  ;;       :addressComponents
  ;;       [{:longText "333",
  ;;         :shortText "333",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "East 60th Street",
  ;;         :shortText "E 60th St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10022",
  ;;         :shortText "10022",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "1505",
  ;;         :shortText "1505",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=2859360265018442261",
  ;;       :internationalPhoneNumber "+1 718-739-6969",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">60-07 Metropolitan Ave</span>, <span class=\"locality\">Queens</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">11385</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "VIXEN", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJh00YQZpewokRFYJK8QJ9ric",
  ;;       :shortFormattedAddress "60-07 Metropolitan Ave, Queens",
  ;;       :formattedAddress "60-07 Metropolitan Ave, Queens, NY 11385, USA",
  ;;       :addressComponents
  ;;       [{:longText "60-07",
  ;;         :shortText "60-07",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Metropolitan Avenue",
  ;;         :shortText "Metropolitan Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Ridgewood",
  ;;         :shortText "Ridgewood",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Queens",
  ;;         :shortText "Queens",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Queens County",
  ;;         :shortText "Queens County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "11385",
  ;;         :shortText "11385",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=4888099127369465316",
  ;;       :internationalPhoneNumber "+1 718-599-2474",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">1089 Grand St</span>, <span class=\"locality\">Brooklyn</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">11211-1702</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "PUMPS", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJoUVqY6xewokR5P3AEUIE1kM",
  ;;       :websiteUri "http://pumpsbar.com/",
  ;;       :shortFormattedAddress "1089 Grand St, Brooklyn",
  ;;       :formattedAddress "1089 Grand St, Brooklyn, NY 11211, USA",
  ;;       :addressComponents
  ;;       [{:longText "1089",
  ;;         :shortText "1089",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Grand Street",
  ;;         :shortText "Grand St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "East Williamsburg",
  ;;         :shortText "East Williamsburg",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Brooklyn",
  ;;         :shortText "Brooklyn",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Kings County",
  ;;         :shortText "Kings County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "11211",
  ;;         :shortText "11211",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "1702",
  ;;         :shortText "1702",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=12133894183818905563",
  ;;       :internationalPhoneNumber "+1 212-901-3240",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">20 W 20th St</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10011</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Wonderland Gentlemen's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ1ci8RChZwokR20e02HhAZKg",
  ;;       :websiteUri "http://nycwonderland.com/",
  ;;       :shortFormattedAddress "20 W 20th St, New York",
  ;;       :formattedAddress "20 W 20th St, New York, NY 10011, USA",
  ;;       :addressComponents
  ;;       [{:longText "20",
  ;;         :shortText "20",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "West 20th Street",
  ;;         :shortText "W 20th St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10011",
  ;;         :shortText "10011",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=6027891456326968011",
  ;;       :internationalPhoneNumber "+1 212-564-4480",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">48 W 33rd St</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10001-3302</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Hoops Cabaret", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJuSs_H6lZwokRyyL7pldfp1M",
  ;;       :websiteUri "http://hoopscabaret.com/",
  ;;       :shortFormattedAddress "48 W 33rd St, New York",
  ;;       :formattedAddress "48 W 33rd St, New York, NY 10001, USA",
  ;;       :addressComponents
  ;;       [{:longText "48",
  ;;         :shortText "48",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "West 33rd Street",
  ;;         :shortText "W 33rd St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10001",
  ;;         :shortText "10001",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "3302",
  ;;         :shortText "3302",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=7693747481442715708",
  ;;       :internationalPhoneNumber "+1 212-391-2702",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">61 W 37th St</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10018</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Vivid Cabaret", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJfUHlNKpZwokRPAjzNpKuxWo",
  ;;       :websiteUri "http://www.vividcabaretny.com/",
  ;;       :shortFormattedAddress "61 W 37th St, New York",
  ;;       :formattedAddress "61 W 37th St, New York, NY 10018, USA",
  ;;       :addressComponents
  ;;       [{:longText "61",
  ;;         :shortText "61",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "West 37th Street",
  ;;         :shortText "W 37th St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10018",
  ;;         :shortText "10018",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=3047390265223268958",
  ;;       :internationalPhoneNumber "+1 718-267-0800",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">26-50 Brooklyn Queens Expy W</span>, <span class=\"locality\">Queens</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">11377</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "NYC Gentlemen's Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJNyAO8xJfwokRXsqd6kyBSio",
  ;;       :websiteUri "https://www.nycgentlemens.club/",
  ;;       :shortFormattedAddress "26-50 Brooklyn Queens Expy W, Queens",
  ;;       :formattedAddress
  ;;       "26-50 Brooklyn Queens Expy W, Queens, NY 11377, USA",
  ;;       :addressComponents
  ;;       [{:longText "26-50",
  ;;         :shortText "26-50",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Brooklyn Queens Expressway West",
  ;;         :shortText "Brooklyn Queens Expy W",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Woodside",
  ;;         :shortText "Woodside",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Queens",
  ;;         :shortText "Queens",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Queens County",
  ;;         :shortText "Queens County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "11377",
  ;;         :shortText "11377",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=2603220565663440796",
  ;;       :internationalPhoneNumber "+1 212-765-5047",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">689 8th Ave</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10036</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "Satin Dolls NYC", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJkUAeeVNYwokRnF9c4E9_ICQ",
  ;;       :websiteUri "http://www.satindollsnyc.com/",
  ;;       :shortFormattedAddress "689 8th Ave, New York",
  ;;       :formattedAddress "689 8th Ave, New York, NY 10036, USA",
  ;;       :addressComponents
  ;;       [{:longText "689",
  ;;         :shortText "689",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "8th Avenue",
  ;;         :shortText "8th Ave",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10036",
  ;;         :shortText "10036",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=4415834895554461760",
  ;;       :internationalPhoneNumber "+1 212-791-5261",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">59 Murray St</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10007</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName {:text "FlashDancers Downtown", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJj51eKxlawokRQEBMAHEySD0",
  ;;       :websiteUri "http://www.flashdancersnyc.com/",
  ;;       :shortFormattedAddress "59 Murray St, New York",
  ;;       :formattedAddress "59 Murray St, New York, NY 10007, USA",
  ;;       :addressComponents
  ;;       [{:longText "59",
  ;;         :shortText "59",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "Murray Street",
  ;;         :shortText "Murray St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10007",
  ;;         :shortText "10007",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri "https://maps.google.com/?cid=7062661584069464252",
  ;;       :internationalPhoneNumber "+1 718-937-6969",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">42-50 21st St</span>, <span class=\"locality\">Queens</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">11101</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Show Palace Gentlemen's Club NYC", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJ1SiUt9dYwokRvBBCh0qdA2I",
  ;;       :websiteUri "http://showpalaceny.com/",
  ;;       :shortFormattedAddress "42-50 21st St, Queens",
  ;;       :formattedAddress "42-50 21st St, Queens, NY 11101, USA",
  ;;       :addressComponents
  ;;       [{:longText "42-50",
  ;;         :shortText "42-50",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "21st Street",
  ;;         :shortText "21st St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Long Island City",
  ;;         :shortText "LIC",
  ;;         :types ["neighborhood" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Queens",
  ;;         :shortText "Queens",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "Queens County",
  ;;         :shortText "Queens County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "11101",
  ;;         :shortText "11101",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}]}
  ;;      {:googleMapsUri
  ;;       "https://maps.google.com/?cid=16117504525731980990",
  ;;       :internationalPhoneNumber "+1 888-363-7795",
  ;;       :adrFormatAddress
  ;;       "<span class=\"street-address\">77 E 7th St Suite #1A</span>, <span class=\"locality\">New York</span>, <span class=\"region\">NY</span> <span class=\"postal-code\">10003-8111</span>, <span class=\"country-name\">USA</span>",
  ;;       :displayName
  ;;       {:text "Golden Boys Male Strip Club", :languageCode "en"},
  ;;       :businessStatus "OPERATIONAL",
  ;;       :id "ChIJQ5nZ69hZwokRvmYB0vjgrN8",
  ;;       :websiteUri "https://officialgoldenboys.com/",
  ;;       :shortFormattedAddress "77 E 7th St Suite #1A, New York",
  ;;       :formattedAddress
  ;;       "77 E 7th St Suite #1A, New York, NY 10003, USA",
  ;;       :addressComponents
  ;;       [{:longText "Suite #1A",
  ;;         :shortText "Suite #1A",
  ;;         :types ["subpremise"],
  ;;         :languageCode "en"}
  ;;        {:longText "77",
  ;;         :shortText "77",
  ;;         :types ["street_number"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "East 7th Street",
  ;;         :shortText "E 7th St",
  ;;         :types ["route"],
  ;;         :languageCode "en"}
  ;;        {:longText "Manhattan",
  ;;         :shortText "Manhattan",
  ;;         :types ["sublocality_level_1" "sublocality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "New York",
  ;;         :types ["locality" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York County",
  ;;         :shortText "New York County",
  ;;         :types ["administrative_area_level_2" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "New York",
  ;;         :shortText "NY",
  ;;         :types ["administrative_area_level_1" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "United States",
  ;;         :shortText "US",
  ;;         :types ["country" "political"],
  ;;         :languageCode "en"}
  ;;        {:longText "10003",
  ;;         :shortText "10003",
  ;;         :types ["postal_code"],
  ;;         :languageCode "en-US"}
  ;;        {:longText "8111",
  ;;         :shortText "8111",
  ;;         :types ["postal_code_suffix"],
  ;;         :languageCode "en-US"}]}]}


  
  ;; => {:places
  ;;     [{:id "ChIJlRqurI3DyIARTwOlBp97nAk",
  ;;       :internationalPhoneNumber "+1 702-366-1141",
  ;;       :formattedAddress "1514 Western Ave, Las Vegas, NV 89102, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=692564365649707855",
  ;;       :websiteUri "http://www.littledarlingsvegas.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Little Darlings Las Vegas", :languageCode "en"}}
  ;;      {:id "ChIJazRM1_TDyIARdQTWWaKyZRI",
  ;;       :internationalPhoneNumber "+1 702-384-0074",
  ;;       :formattedAddress "2112 Western Ave, Las Vegas, NV 89102, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=1325662075692123253",
  ;;       :websiteUri "https://thelibrarygc.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "The Library Gentlemen's Club", :languageCode "en"}}
  ;;      {:id "ChIJj8bp9xXEyIARCca4_DIUblw",
  ;;       :internationalPhoneNumber "+1 702-869-0003",
  ;;       :formattedAddress
  ;;       "3025 S Sammy Davis Jr Dr, Las Vegas, NV 89109, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=6660283108148430345",
  ;;       :websiteUri "http://www.sapphirelasvegas.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Sapphire Las Vegas Gentlemen's Club", :languageCode "en"}}
  ;;      {:id "ChIJn6Izpg_EyIAROINF4x7zlmo",
  ;;       :internationalPhoneNumber "+1 702-916-1499",
  ;;       :formattedAddress "2995 S Highland Dr, Las Vegas, NV 89109, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=7680593528514511672",
  ;;       :websiteUri "https://scoreslv.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Scores Las Vegas Gentleman's Club", :languageCode "en"}}
  ;;      {:id "ChIJf2WtVRDEyIARkBfcdqlNff4",
  ;;       :internationalPhoneNumber "+1 702-894-4167",
  ;;       :formattedAddress
  ;;       "3247 S Sammy Davis Jr Dr, Las Vegas, NV 89109, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=18337898647961474960",
  ;;       :websiteUri "http://dejavuvegas.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Deja Vu Showgirls Las Vegas", :languageCode "en"}}
  ;;      {:id "ChIJ7a6ZYaDGyIARab2EPir5LT8",
  ;;       :internationalPhoneNumber "+1 702-890-3731",
  ;;       :formattedAddress
  ;;       "3750 S Valley View Blvd, Las Vegas, NV 89103, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=4552568758174268777",
  ;;       :websiteUri "http://honeyslasvegas.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Honeys Gentlemen’s Club Las Vegas", :languageCode "en"}}
  ;;      {:id "ChIJV3syXRrEyIARRrsuX85xHHY",
  ;;       :internationalPhoneNumber "+1 702-796-3600",
  ;;       :formattedAddress "3340 S Highland Dr, Las Vegas, NV 89109, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=8510802526997494598",
  ;;       :websiteUri "http://www.spearmintrhinolv.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Spearmint Rhino Gentlemen's Club Las Vegas",
  ;;        :languageCode "en"}}
  ;;      {:id "ChIJo0LE7R7FyIARGe2JI3ib_Yw",
  ;;       :internationalPhoneNumber "+1 702-710-7419",
  ;;       :formattedAddress
  ;;       "2217 Paradise Rd suite b, Las Vegas, NV 89104, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=10159447274712329497",
  ;;       :websiteUri "http://stripclubpluglv.com/book-now/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "Strip Club Plug LV", :languageCode "en"}}
  ;;      {:id "ChIJYdREcYjDyIARIpZCszHtMjc",
  ;;       :internationalPhoneNumber "+1 702-642-2984",
  ;;       :formattedAddress
  ;;       "1848 Las Vegas Blvd N, North Las Vegas, NV 89030, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=3977502218638235170",
  ;;       :websiteUri
  ;;       "https://palominolv.com/?utm_source=google&utm_medium=organic&utm_campaign=GMB-Las-Vegas",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Palomino Las Vegas Gentlemen's Club", :languageCode "en"}}
  ;;      {:id "ChIJE6g-egnEyIARhKKW6F79i1g",
  ;;       :internationalPhoneNumber "+1 702-257-3030",
  ;;       :formattedAddress "2801 Westwood Dr, Las Vegas, NV 89109, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=6380471881171378820",
  ;;       :websiteUri
  ;;       "https://treasureslasvegas.com/?utm_source=Google&utm_medium=GBP&utm_campaign=Local_seo&utm_id=Local_SEO",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Treasures Gentlemen's Club & Steakhouse",
  ;;        :languageCode "en"}}
  ;;      {:id "ChIJV1rlcgfDyIAR0lHAxrlktBw",
  ;;       :internationalPhoneNumber "+1 702-473-9977",
  ;;       :formattedAddress
  ;;       "1531 Las Vegas Blvd S, Las Vegas, NV 89104, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=2068388877936120274",
  ;;       :websiteUri
  ;;       "https://www.thepepperminthippo.com/locations/las-vegas-nv/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "Peppermint Hippo", :languageCode "en"}}
  ;;      {:id "ChIJpWNdAA_FyIARdGf5mzkghbs",
  ;;       :internationalPhoneNumber "+1 702-463-4127",
  ;;       :formattedAddress "3177 S Highland Dr, Las Vegas, NV 89109, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=13512241688797079412",
  ;;       :websiteUri "https://diamondcabaretvegas.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "Diamond Cabaret", :languageCode "en"}}
  ;;      {:id "ChIJiar7EpzDyIAREtY3u-wz2N8",
  ;;       :internationalPhoneNumber "+1 702-755-1766",
  ;;       :formattedAddress
  ;;       "3585 S Highland Dr #46, Las Vegas, NV 89103, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=16129699157273728530",
  ;;       :websiteUri "https://www.stripclubconcierge.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Strip Club Concierge Las Vegas", :languageCode "en"}}
  ;;      {:id "ChIJLQgJP7fDyIARvfikt7F5AoE",
  ;;       :internationalPhoneNumber "+1 702-306-5083",
  ;;       :formattedAddress
  ;;       "300 S 4th St Ste 661B, Las Vegas, NV 89101, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=9296126385043339453",
  ;;       :websiteUri
  ;;       "https://www.stripclubconcierge.com/downtown-las-vegas/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Strip Club Concierge Las Vegas Downtown",
  ;;        :languageCode "en"}}
  ;;      {:id "ChIJGdDCq_nDyIARceO_NR7FQR8",
  ;;       :internationalPhoneNumber "+1 725-279-8384",
  ;;       :formattedAddress
  ;;       "211 N 8th St unit 358, Las Vegas, NV 89101, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=2252298022203417457",
  ;;       :websiteUri "https://stripclubplug.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "STRIP CLUB PLUG", :languageCode "en"}}
  ;;      {:id "ChIJ17-s36XGyIARBTPqHba2hG4",
  ;;       :internationalPhoneNumber "+1 702-876-1550",
  ;;       :formattedAddress
  ;;       "4120 Spring Mountain Rd, Las Vegas, NV 89102, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=7963690934400070405",
  ;;       :websiteUri "http://www.playitagainsams.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "Play It Again Sam's", :languageCode "en"}}
  ;;      {:id "ChIJ7wd081LEyIARuaJpqp2LDSk",
  ;;       :internationalPhoneNumber "+1 702-734-7990",
  ;;       :formattedAddress "4416 Paradise Rd, Las Vegas, NV 89169, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=2958174039537459897",
  ;;       :websiteUri "https://centerfoldslasvegas.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Centerfolds Cabaret Las Vegas", :languageCode "en"}}
  ;;      {:id "ChIJ4SBNjgLDyIAR_btXmW1BUK8",
  ;;       :internationalPhoneNumber "+1 702-256-7894",
  ;;       :formattedAddress
  ;;       "1818 Las Vegas Blvd N, North Las Vegas, NV 89030, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=12632668893754145789",
  ;;       :websiteUri
  ;;       "https://chicasbonitaslocations.vdigitalservices.com/?utm_source=gmb&utm_medium=referral",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "Chicas Bonitas", :languageCode "en"}}
  ;;      {:id "ChIJlbxGv7DFyIARnSi5v_b_MQ0",
  ;;       :internationalPhoneNumber "+1 702-628-2060",
  ;;       :formattedAddress
  ;;       "3960 Howard Hughes Pkwy Suite 563-02, Las Vegas, NV 89169, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=950822431595505821",
  ;;       :websiteUri "https://vegasbeststripclub.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Vegas Best Strip Club Service", :languageCode "en"}}
  ;;      {:id "ChIJu5gCb1fbyIARMuGJhIIuq3s",
  ;;       :internationalPhoneNumber "+1 702-780-5939",
  ;;       :formattedAddress "3785 Boulder Hwy, Las Vegas, NV 89121, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=8911267425786913074",
  ;;       :websiteUri "http://thelvlv.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "LVLV Gentlemen's Club", :languageCode "en"}}]}


  
  ;; => {:places
  ;;     [{:id "ChIJ1SiUt9dYwokRvBBCh0qdA2I",
  ;;       :internationalPhoneNumber "+1 718-937-6969",
  ;;       :formattedAddress "42-50 21st St, Queens, NY 11101, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=7062661584069464252",
  ;;       :websiteUri "http://showpalaceny.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Show Palace Gentlemen's Club NYC", :languageCode "en"}}
  ;;      {:id "ChIJS63zgLFZwokRr7zAcTzZvns",
  ;;       :internationalPhoneNumber "+1 212-421-3600",
  ;;       :formattedAddress "1674 Broadway, New York, NY 10019, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=8916803165869882543",
  ;;       :websiteUri "http://www.sapphiretimessquare.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Sapphire Times Square NYC Gentlemen's Club",
  ;;        :languageCode "en"}}
  ;;      {:id "ChIJ2VhDH6lZwokR_acLQfV63Tc",
  ;;       :internationalPhoneNumber "+1 212-372-0850",
  ;;       :formattedAddress "50 W 33rd St, New York, NY 10001, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=4025508835715950589",
  ;;       :websiteUri "https://ricksnewyork.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Rick's Cabaret New York", :languageCode "en"}}
  ;;      {:id "ChIJi32IRNBZwokRrk85gVDZN50",
  ;;       :internationalPhoneNumber "+1 212-922-0995",
  ;;       :formattedAddress "622 W 47th St, New York, NY 10036, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=11328762327462399918",
  ;;       :websiteUri "https://www.spearmintrhinonyc.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Spearmint Rhino Gentlemen's Club New York City",
  ;;        :languageCode "en"}}
  ;;      {:id "ChIJ35cNMJBZwokRdSACJ4WdurQ",
  ;;       :internationalPhoneNumber "+1 212-247-2460",
  ;;       :formattedAddress "641 W 51st St, New York, NY 10019, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=13022894467705872501",
  ;;       :websiteUri "https://hustlerny.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Hustler Club NYC | Best New York Strip Club",
  ;;        :languageCode "en"}}
  ;;      {:id "ChIJ6_2TsExYwokR4fqIztY5o_M",
  ;;       :internationalPhoneNumber "+1 917-397-5953",
  ;;       :formattedAddress "552 W 38th St, New York, NY 10018, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=17555939367171193569",
  ;;       :websiteUri "http://hqkony.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "HQ KONY Gentlemen's Club", :languageCode "en"}}
  ;;      {:id "ChIJUUUMPrlZwokRRfoSuC_ndas",
  ;;       :internationalPhoneNumber "+1 212-764-6969",
  ;;       :formattedAddress "725 7th Ave, New York, NY 10019, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=12355035344898554437",
  ;;       :websiteUri "http://www.platinumdollsnyc.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Platinum Dolls Gentlemen's Club & Sports Bar",
  ;;        :languageCode "en"}}
  ;;      {:id "ChIJ2cl8hzRZwokRkYGy_OGvuJ0",
  ;;       :internationalPhoneNumber "+1 646-663-5130",
  ;;       :formattedAddress "Times Square, New York, NY 10036, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=11365027044811506065",
  ;;       :websiteUri "https://exotiquemen.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Exotique Men Male Strip Club", :languageCode "en"}}
  ;;      {:id "ChIJgVaT1VdYwokRtR8BSg5Nueo",
  ;;       :internationalPhoneNumber "+1 212-315-5107",
  ;;       :formattedAddress "320 W 45th St, New York, NY 10036, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=16913634599333076917",
  ;;       :websiteUri "http://www.flashdancersnyc.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "Flashdancers NYC", :languageCode "en"}}
  ;;      {:id "ChIJWVaNiRxZwokRuoArKDXxNtI",
  ;;       :internationalPhoneNumber "+1 212-381-6197",
  ;;       :formattedAddress "501 E 89th St, New York, NY 10128, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=15147559607316742330",
  ;;       :websiteUri
  ;;       "https://www.thebachelorpartyhookup.com/sapphire-new-york-nyc-strip-club",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Strippers NYC - The Bachelor Party Strippers Hookup",
  ;;        :languageCode "en"}}
  ;;      {:id "ChIJ1ci8RChZwokR20e02HhAZKg",
  ;;       :internationalPhoneNumber "+1 212-901-3240",
  ;;       :formattedAddress "20 W 20th St, New York, NY 10011, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=12133894183818905563",
  ;;       :websiteUri "http://nycwonderland.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Wonderland Gentlemen's Club", :languageCode "en"}}
  ;;      {:id "ChIJj51eKxlawokRQEBMAHEySD0",
  ;;       :internationalPhoneNumber "+1 212-791-5261",
  ;;       :formattedAddress "59 Murray St, New York, NY 10007, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=4415834895554461760",
  ;;       :websiteUri "http://www.flashdancersnyc.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "FlashDancers Downtown", :languageCode "en"}}
  ;;      {:id "ChIJDSmv5a9ZwokRWGdSQnUMY3w",
  ;;       :internationalPhoneNumber "+1 917-513-2484",
  ;;       :formattedAddress "229 W 28th St, New York, NY 10001, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=8963021381160822616",
  ;;       :websiteUri "http://www.getpunished.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Get Punished Male Strip Club NYC of Male Strippers",
  ;;        :languageCode "en"}}
  ;;      {:id "ChIJJQZ4v9BZwokRJOsfahVT3sg",
  ;;       :internationalPhoneNumber "+1 929-930-3446",
  ;;       :formattedAddress "155 W 71st St, New York, NY 10023, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=14474097603855248164",
  ;;       :websiteUri "https://www.mangobartsq.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Mango's Male Strip Club NYC for Women",
  ;;        :languageCode "en"}}
  ;;      {:id "ChIJ15X9N-ZYwokRcbF-sABC4kg",
  ;;       :internationalPhoneNumber "+1 212-421-3600",
  ;;       :formattedAddress "333 E 60th St, New York, NY 10022, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=5251832686195945841",
  ;;       :websiteUri "http://www.nysapphire.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "Sapphire New York", :languageCode "en"}}
  ;;      {:id "ChIJOTmUV7ZZwokR7VXsJXwtqWU",
  ;;       :internationalPhoneNumber "+1 212-868-4900",
  ;;       :formattedAddress "536 W 28th St, New York, NY 10001, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=7325436280130262509",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "SCORES GENTLEMEN'S & STEAKHOUSE", :languageCode "en"}}
  ;;      {:id "ChIJ0_qKRaNZwokRh19FEbzS67c",
  ;;       :internationalPhoneNumber "+1 212-299-5274",
  ;;       :formattedAddress "621 W 46th St, New York, NY 10036, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=13252918033653260167",
  ;;       :websiteUri
  ;;       "https://www.hunkomanianyc.com/new-york-city-male-strip-club.html",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Male Strip Club NYC of Male Strippers NYC - Hunk-O-Mania",
  ;;        :languageCode "en"}}
  ;;      {:id "ChIJkUAeeVNYwokRnF9c4E9_ICQ",
  ;;       :internationalPhoneNumber "+1 212-765-5047",
  ;;       :formattedAddress "689 8th Ave, New York, NY 10036, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=2603220565663440796",
  ;;       :websiteUri "http://www.satindollsnyc.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "Satin Dolls NYC", :languageCode "en"}}
  ;;      {:id "ChIJNyAO8xJfwokRXsqd6kyBSio",
  ;;       :internationalPhoneNumber "+1 718-267-0800",
  ;;       :formattedAddress
  ;;       "26-50 Brooklyn Queens Expy W, Queens, NY 11377, USA",
  ;;       :googleMapsUri "https://maps.google.com/?cid=3047390265223268958",
  ;;       :websiteUri "https://www.nycgentlemens.club/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName {:text "NYC Gentlemen's Club", :languageCode "en"}}
  ;;      {:id "ChIJQ5nZ69hZwokRvmYB0vjgrN8",
  ;;       :internationalPhoneNumber "+1 888-363-7795",
  ;;       :formattedAddress
  ;;       "77 E 7th St Suite #1A, New York, NY 10003, USA",
  ;;       :googleMapsUri
  ;;       "https://maps.google.com/?cid=16117504525731980990",
  ;;       :websiteUri "https://officialgoldenboys.com/",
  ;;       :businessStatus "OPERATIONAL",
  ;;       :displayName
  ;;       {:text "Golden Boys Male Strip Club", :languageCode "en"}}]}

  )


(defn parse-address [id s]
  (let [lst (as-> s $
              (cstr/split $ #",")
              (mapv cstr/trim $))
        rev (vec (reverse lst))
        country (first rev)
        unit? (< 4 (count lst))
        [state zip] (as-> rev $
                      (get $ 1)
                      (cstr/split $ #" ")
                      (mapv cstr/trim $))
        city (get rev 2)
        street (if unit?
                 (->> (take 2 lst)
                      (cstr/join ", "))
                 (first lst))]
    #:address{:id id :street street
              :city city :state state
              :zip zip :country country}))

(defn transform-gplace
  [industry
   {:keys [googleMapsUri internationalPhoneNumber
           websiteUri displayName businessStatus
           id formattedAddress]}]
  (let [address-id (u/uuid)
        address (parse-address address-id formattedAddress)
        place-id (u/uuid)
        label (:text displayName)
        status (-> businessStatus cstr/lower-case keyword)
        place (merge {:place/id place-id
                      :place/industry industry
                      :place/language :en
                      :place/label label
                      :place/handle (calc/handlify address label)
                      :place/status status
                      :place/address-id address-id
                      :place/google-id id
                      :place/google-uri googleMapsUri}
                     (when websiteUri {:place/website websiteUri})
                     (when internationalPhoneNumber
                       {:place/phone internationalPhoneNumber}))]
    [place address]))

(def transit
  (atom nil))

(defn store
  "record places by state into db"
  [industry text-query]
  (let [google-data (request text-query)
        transform-f (partial transform-gplace industry)
        tia-data (->> google-data
                      (map transform-f)
                      (apply concat) vec)]
    (reset! transit tia-data)))

(-> transit deref second)
;; => #:address{:id #uuid "23187fbc-464b-4c34-a7d8-12ee6674803a",
;;              :street "40 Brighton Ave",
;;              :city "Passaic",
;;              :state "NJ",
;;              :zip "07055",
;;              :country "USA"}
;; => #:place{:phone "+1 973-365-0373",
;;            :address-id #uuid "23187fbc-464b-4c34-a7d8-12ee6674803a",
;;            :google-uri
;;            "https://maps.google.com/?cid=7775269342707746748",
;;            :language :en,
;;            :status :operational,
;;            :id #uuid "24bc1440-1e14-4350-b00c-cdb979cfcb73",
;;            :google-id "ChIJvcuTEGX_wokRvIcUpUVO52s",
;;            :website "http://www.silkgentlemensclub.com/",
;;            :label "Silk Gentlemens Club",
;;            :industry :strip-club,
;;            :handle :silk-gentlemens-club}

(comment
  (store :strip-club
         "strip clubs in passaic county, new jersey")
  (count @transit)
  (count (filter #(:place/handle %) @transit))
  (count (filter #(:address/street %) @transit))
  (->> (filter #(:place/handle %) @transit)
       (map #(m/validate model/place %))
       (every? true?))
  (->> (filter #(:address/street %) @transit)
       (map #(m/validate model/address %))
       (every? true?))
  (->> @transit
       (mapv :place/handle)
       (remove nil?) vec)
  (->> @transit
       (mapv :address/city)
       (remove nil?) vec)
  (->> @transit
       (mapv :address/state)
       (remove nil?) vec)
  (doseq [m @transit]
    (dbc/upsert! m))
  (dbc/count-all-having-key
   :address/id)
  (dbc/count-all-having-key
   :place/id))
