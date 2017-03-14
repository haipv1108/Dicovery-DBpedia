package com.brine.discovery.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import static com.brine.discovery.util.Config.BASE_URL_DBPEDIA;
import static com.brine.discovery.util.Config.RESULT_JSON_TYPE;

/**
 * Created by phamhai on 08/02/2017.
 */

public class Utils {
    private static final String TAG = Utils.class.getCanonicalName();
    private static final String FS_SEARCH_BASE_URL = "http://dbpedia.org/sparql?default-graph-uri=&query=";

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
            url = Config.SOUNDCLOUD_URL + URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        showLog("soundcloud url: " + url);
        return url;
    }

    public static String createUrlFacetedSearch(String keyword, String optionSearch){
        String query = createQueryFacetedSearch(keyword, optionSearch);
        String url = "";
        try {
            url = FS_SEARCH_BASE_URL + URLEncoder.encode(query, "UTF-8") + RESULT_JSON_TYPE;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        showLog(url);
        return url;
    }

    private static String createQueryFacetedSearch(String keyword, String optionSearch){
        String bifVectorParams = getBifVectorParams(keyword);
        String bifContainParams = getBifContainParams(keyword);

        String query = "select distinct ?s1 as ?c1, (bif:search_excerpt (bif:vector (" + bifVectorParams + "), ?o1)) as ?c2, ?sc, ?rank, ?g where {{{ select ?s1, (?sc * 3e-1) as ?sc, ?o1, (sql:rnk_scale (<LONG::IRI_RANK> (?s1))) as ?rank, ?g where  \n" +
                "  { \n" +
                "    quad map virtrdf:DefaultQuadMap \n" +
                "    { \n" +
                "      graph ?g \n" +
                "      { \n" +
                "         ?s1 ?s1textp ?o1 .\n" +
                "        ?o1 bif:contains  '(" + bifContainParams + ")'  option (score ?sc)  .\n" +
                "        \n" +
                "      }\n" +
                "     }\n" +
                "    "  + optionSearch + "\n" +
                "  }\n" +
                " order by desc (?sc * 3e-1 + sql:rnk_scale (<LONG::IRI_RANK> (?s1)))  limit 50  offset 0 }}} ";
        showLog(query);
        return query;
    }

    public static String createUrlGetTypes(String keyword){
        String query = createQueryGetAllType(keyword);
        String url = "";
        try {
            url = FS_SEARCH_BASE_URL + URLEncoder.encode(query, "UTF-8") + RESULT_JSON_TYPE;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        showLog(url);
        return url;
    }

    private static String createQueryGetAllType(String keyword){
        String bifContainParams = getBifContainParams(keyword);

        String query = "select ?s1c as ?c1 count (distinct (?s1)) as ?c2  where  \n" +
                "  { \n" +
                "    quad map virtrdf:DefaultQuadMap \n" +
                "    { \n" +
                "      graph ?g \n" +
                "      { \n" +
                "         ?s1 ?s1textp ?o1 .\n" +
                "        ?o1 bif:contains  '(" + bifContainParams + ")'  .\n" +
                "        \n" +
                "      }\n" +
                "     }\n" +
                "    ?s1 a ?s1c .\n" +
                "    FILTER (!regex(?s1c, \"yago\",\"i\")) .\n" +
                "    FILTER (!regex(?s1c, \"wikidata\",\"i\")) .\n" +
                "    FILTER (regex(?s1c, \"dbpedia\",\"i\")) .\n" +
                "    \n" +
                "  }\n" +
                " group by ?s1c order by desc 2 limit 50  offset 0  ";
        showLog(query);
        return query;
    }

    public static String createUrlGetAttributes(String keywordSearch){
        String query = createQueryGetAllAttribute(keywordSearch);
        String url = "";
        try {
            url = FS_SEARCH_BASE_URL + URLEncoder.encode(query, "UTF-8") + RESULT_JSON_TYPE;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        showLog(url);
        return url;
    }

    private static String createQueryGetAllAttribute(String keywordSearch){
        String bifContainParams = getBifContainParams(keywordSearch);

        String query = "select ?s1p as ?c1 count (*) as ?c2  where  \n" +
                "  { \n" +
                "    quad map virtrdf:DefaultQuadMap \n" +
                "    { \n" +
                "      graph ?g \n" +
                "      { \n" +
                "         ?s1 ?s1textp ?o1 .\n" +
                "        ?o1 bif:contains  '(" + bifContainParams + ")'  .\n" +
                "        \n" +
                "      }\n" +
                "     }\n" +
                "    ?s1 ?s1p ?s1o .\n" +
                "    FILTER (regex(?s1p, \"dbpedia\",\"i\")) .\n" +
                "    \n" +
                "  }\n" +
                " group by ?s1p order by desc 2 limit 50  offset 0  ";
        showLog(query);
        return query;
    }

    public static String createUrlGetValues(String keywordSearch){
        String query = createQueryGetAllValue(keywordSearch);
        String url = "";
        try {
            url = FS_SEARCH_BASE_URL + URLEncoder.encode(query, "UTF-8") + RESULT_JSON_TYPE;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        showLog(url);
        return url;
    }

    private static String createQueryGetAllValue(String keywordSearch){
        String bifContainParams = getBifContainParams(keywordSearch);

        String query = "     select ?s1ip as ?c1 count (*) as ?c2  where  \n" +
                "  { \n" +
                "    quad map virtrdf:DefaultQuadMap \n" +
                "    { \n" +
                "      graph ?g \n" +
                "      { \n" +
                "         ?s1 ?s1textp ?o1 .\n" +
                "        ?o1 bif:contains  '(" + bifContainParams + ")'  .\n" +
                "        \n" +
                "      }\n" +
                "     }\n" +
                "    ?s1o ?s1ip ?s1 .\n" +
                "    FILTER (regex(?s1ip, \"dbpedia\",\"i\")) .\n" +
                "  }\n" +
                " group by ?s1ip order by desc 2 limit 30  offset 0  ";
        showLog(query);
        return query;
    }

    public static String createUrlGetDistincts(String keywordSearch){
        String query = createQueryGetAllDistinct(keywordSearch);
        String url = "";
        try {
            url = FS_SEARCH_BASE_URL + URLEncoder.encode(query, "UTF-8") + RESULT_JSON_TYPE;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        showLog(url);
        return url;
    }

    private static String createQueryGetAllDistinct(String keywordSearch){
        String bifContainParams = getBifContainParams(keywordSearch);

        String query = "     select ?s1 as ?c1 count (*) as ?c2 where \n" +
                "  { \n" +
                "    select distinct ?s1 ?g  \n" +
                "    { \n" +
                "      quad map virtrdf:DefaultQuadMap \n" +
                "      { \n" +
                "        graph ?g \n" +
                "        { \n" +
                "           ?s1 ?s1textp ?o1 .\n" +
                "          ?o1 bif:contains  '(" + bifContainParams + ")'  .\n" +
                "          \n" +
                "        }\n" +
                "       }\n" +
                "      \n" +
                "    }\n" +
                "   }\n" +
                " group by ?s1 order by desc 2 limit 50  offset 0  ";
        showLog(query);
        return query;
    }


    private static String getBifVectorParams(String keywordSearch){
        List<String> listWord = Arrays.asList(keywordSearch.split(" "));
        String bifVectorParams = "";
        for(int i = 0; i < listWord.size(); i++){
            if(bifVectorParams.length() == 0){
                bifVectorParams += "'" + listWord.get(i).toUpperCase() + "'";
            }else {
                bifVectorParams += ", " + "'" + listWord.get(i).toUpperCase() + "'";
            }
        }
        showLog("Vector param: " + bifVectorParams);
        return bifVectorParams;
    }

    private static String getBifContainParams(String keywordSearch){
        List<String> listWord = Arrays.asList(keywordSearch.split(" "));
        String bifContainParams = "";
        for(int i = 0; i < listWord.size(); i++){
            if(bifContainParams.length() == 0){
                bifContainParams += listWord.get(i).toUpperCase();
            }else {
                bifContainParams += " AND " + listWord.get(i).toUpperCase();
            }
        }
        showLog("Contain param: " + bifContainParams);
        return bifContainParams;
    }

    private static void showLog(String message){
        Log.d(TAG, message);
    }
}
