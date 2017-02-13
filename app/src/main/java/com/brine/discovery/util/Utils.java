package com.brine.discovery.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static com.brine.discovery.util.Config.BASE_URL_DBPEDIA;
import static com.brine.discovery.util.Config.RESULT_JSON_TYPE;

/**
 * Created by phamhai on 08/02/2017.
 */

public class Utils {
    private static final String TAG = Utils.class.getCanonicalName();

    public static String createUrlKeywordSearch(String queryString){
        String url = Config.LOOKUP_DBPEDIA + "QueryClass=&MaxHits=30&QueryString=" + queryString;
        showLog("Lookup url: " + url);
        return url;
    }

    public static String createUrlDbpedia(String query){
        String url = "";
        try {
            url = BASE_URL_DBPEDIA + URLEncoder.encode(query, "UTF-8") + RESULT_JSON_TYPE;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        showLog(url);
        return url;
    }

    public static String createUrlGetDetailsUri(String uri, List<String> fromUris){
        String optional = "";
        for(int i = 0; i < fromUris.size(); i++){
            String fromUri = fromUris.get(i);
            optional += "OPTIONAL {<" + fromUri + "> rdfs:label ?from" + i + "} .\n" +
                    "FILTER ( lang(?from" + i + ") = 'en' )\n";
        }
        showLog("Get details uri: " + optional);
        String query = "SELECT DISTINCT *\n" +
                "WHERE\n" +
                "{\n" +
                "<" + uri + "> rdfs:label ?label .\n" +
                "OPTIONAL \n" +
                "{\n" +
                "<" + uri + "> <http://dbpedia.org/ontology/abstract> ?description .\n" +
                "FILTER ( lang(?description) = \"en\" )\n" +
                "}\n" +
                "\n" +
                "OPTIONAL {<" + uri + "> foaf:depiction ?image} .\n" +
                "OPTIONAL {<" + uri + "> <http://dbpedia.org/ontology/thumbnail> ?thumb} .\n" +
                "FILTER ( lang(?label) = \"en\" ) .\n" +
                optional +
                "}";
        String url = createUrlDbpedia(query);
        showLog("Get details uri: " + url);
        return url;
    }

    public static String createUrlGetTypeUri(String uri){
        String query = "SELECT *\n" +
                "WHERE\n" +
                "{\n" +
                "<" + uri + "> ?y ?z .\n" +
                "?y rdfs:label ?type .\n" +
                "?z rdfs:label ?name .\n" +
                "FILTER ( lang(?type) = \"en\" && lang(?name) = \"en\" && !regex(?z, \"List_of_\") && ?y!=<http://dbpedia.org/ontology/wikiPageWikiLink> )\n" +
                "}";
        String url = createUrlDbpedia(query);
        showLog("Get type uri: " + url);
        return url;
    }

    public static String createUrlGetVideoYoutube(String keywordSearch){
        String url = Config.GOOGLE_URL + "q=" + keywordSearch + "&key=" + Config.YOUTUBE_API_KEY;
        showLog("youtube url: " + url);
        return url;
    }

    public static String createUrlGetSoundCloud(String keyword){
        String url = "";
        try {
            url = Config.SOUNDCLOUD_API + URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        showLog("soundcloud url: " + url);
        return url;
    }

    private static void showLog(String message){
        Log.d(TAG, message);
    }
}
