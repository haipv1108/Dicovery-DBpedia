package com.brine.discovery.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by phamhai on 08/02/2017.
 */

public class DbpediaConstant {

    private ArrayList<String> albums = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Album");
        add("http://dbpedia.org/ontology/Musical");
        add("http://dbpedia.org/ontology/MusicGenre");
        add("http://dbpedia.org/ontology/MusicalWork");
        add("http://dbpedia.org/ontology/Song");
    }};

    private ArrayList<String> artists = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Artist");
        add("http://dbpedia.org/ontology/Actor");
        add("http://dbpedia.org/ontology/AdultActor");
        add("http://dbpedia.org/ontology/MusicalArtist");
        add("http://dbpedia.org/ontology/Writer");
        add("http://dbpedia.org/ontology/Comedian");
        add("http://dbpedia.org/ontology/Philosopher");
    }};

    private ArrayList<String> arts_humanities = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Work");
        add("http://dbpedia.org/ontology/MusicalWork");
        add("http://dbpedia.org/ontology/WrittenWork");
        add("http://dbpedia.org/ontology/Museum");
        add("http://schema.org/Painting");
        add("http://dbpedia.org/ontology/Philosopher");
        add("http://dbpedia.org/ontology/Sculpture");
        add("http://dbpedia.org/ontology/Theatre");
    }};

    private ArrayList<String> books = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Book");
        add("http://dbpedia.org/ontology/AcademicJournal");
        add("http://dbpedia.org/ontology/PeriodicalLiterature");
        add("http://dbpedia.org/ontology/Writer");
        add("http://dbpedia.org/ontology/WrittenWork");
    }};

    private ArrayList<String> community = new ArrayList<String>();

    private ArrayList<String> company = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Company");
    }};

    private ArrayList<String> computers = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Device");
        add("http://dbpedia.org/ontology/Database");
        add("http://dbpedia.org/ontology/ProgrammingLanguage");
        add("http://dbpedia.org/ontology/Device");
        add("http://dbpedia.org/ontology/Software");
    }};

    private ArrayList<String> computers_internet = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Website");
        add("http://dbpedia.org/ontology/Database");
    }};

    private ArrayList<String> computers_technology = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Software");
        add("http://dbpedia.org/ontology/Database");
        add("http://dbpedia.org/ontology/ProgrammingLanguage");
    }};

    private ArrayList<String> food_beverages = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Food");
    }};

    private ArrayList<String> games_toys = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Game");
        add("http://dbpedia.org/ontology/VideoGame");
        add("http://dbpedia.org/ontology/VideogamesLeague");
        add("http://dbpedia.org/ontology/Play");
    }};

    private ArrayList<String> homedocer = new ArrayList<>();

    private ArrayList<String> internet_software = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Company");
        add("http://dbpedia.org/ontology/Website");
    }};

    private ArrayList<String> magazine = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Magazine");
    }};

    private ArrayList<String> movie = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Film");
        add("http://dbpedia.org/ontology/Actor");
        add("http://dbpedia.org/ontology/AdultActor");
        add("http://dbpedia.org/ontology/Comedian");
        add("http://dbpedia.org/ontology/FictionalCharacter");
        add("http://dbpedia.org/ontology/FilmFestival");
    }};

    private ArrayList<String> movie_music = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Song");
    }};

    private ArrayList<String> musician_band = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Band");
        add("http://dbpedia.org/ontology/Artist");
        add("http://dbpedia.org/ontology/MusicalArtist");
        add("http://dbpedia.org/ontology/MusicFestival");
    }};

    private ArrayList<String> personalblog = new ArrayList<String>();

    private ArrayList<String> product_service = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Company");
        add("http://dbpedia.org/ontology/Software");
    }};

    private ArrayList<String> publicfigure = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Artist");
        add("http://dbpedia.org/ontology/Actor");
        add("http://dbpedia.org/ontology/AdultActor");
        add("http://dbpedia.org/ontology/Comedian");
        add("http://dbpedia.org/ontology/Journalist");
        add("http://dbpedia.org/ontology/Person");
        add("http://dbpedia.org/ontology/Philosopher");
        add("http://dbpedia.org/ontology/Politician");
        add("http://dbpedia.org/ontology/President");
        add("http://dbpedia.org/ontology/OfficeHolder");
        add("http://dbpedia.org/ontology/PrimeMinister");
    }};

    private ArrayList<String> retailandonsumermerchandise = new ArrayList<>();

    private ArrayList<String> software = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Software");
        add("http://dbpedia.org/ontology/ProgrammingLanguage");
        add("http://dbpedia.org/ontology/Company");
    }};

    private ArrayList<String> travel_leisure = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Company");
        add("http://dbpedia.org/ontology/City");
        add("http://dbpedia.org/ontology/Country");
        add("http://dbpedia.org/ontology/Hotel");
        add("http://dbpedia.org/ontology/Mountain");
        add("http://dbpedia.org/ontology/Museum");
        add("http://dbpedia.org/ontology/Park");
        add("http://dbpedia.org/ontology/Place");
        add("http://dbpedia.org/ontology/Restaurant");
        add("http://dbpedia.org/ontology/SkiArea");
        add("http://dbpedia.org/ontology/Town");
        add("http://dbpedia.org/ontology/Village");
    }};

    private ArrayList<String> tvnetwork = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/TelevisionEpisode");
        add("http://dbpedia.org/ontology/TelevisionSeason");
        add("http://dbpedia.org/ontology/TelevisionShow");
        add("http://dbpedia.org/ontology/Company");
    }};

    private ArrayList<String> tvshow = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/TelevisionEpisode");
        add("http://dbpedia.org/ontology/TelevisionSeason");
        add("http://dbpedia.org/ontology/TelevisionShow");
    }};

    private ArrayList<String> unknown = new ArrayList<>();

    private ArrayList<String> website = new ArrayList<String>(){{
        add("http://dbpedia.org/ontology/Website");
    }};

    private ArrayList<String> other = new ArrayList<String>(){{
        add("http://www.w3.org/2002/07/owl#Thing");
    }};

    Map<String, ArrayList<String>> ontologyMap = new HashMap<String, ArrayList<String>>(){{
        put("album", albums);
        put("artist", artists);
        put("arts/humanities", arts_humanities);
        put("book", books);
        put("community", community);
        put("company", company);
        put("computers", computers);
        put("computers/internet", computers_internet);
        put("computers/technology", computers_technology);
        put("food/beverages", food_beverages);
        put("games/toys", games_toys);
        put("home decor", homedocer);
        put("software", internet_software);
        put("magazine", magazine);
        put("movie", movie);
        put("movie/music", movie_music);
        put("musician/band", musician_band);
        put("personal blog", personalblog);
        put("product/service", product_service);
        put("public figure", publicfigure);
        put("retail and consumer merchandise", retailandonsumermerchandise);
        put("software", software);
        put("travel/leisure", travel_leisure);
        put("tv network", tvnetwork);
        put("tv show", tvshow);
        put("unknown", unknown);
        put("website", website);
        put("other", other);
    }};

    public static boolean isContext(String uriInput) {
        List<String> contextMusic = Arrays.asList(
                "Song",
                "Film",
                "Band",
                "Album",
                "MusicalArtist",
                "MusicGenre",
                "Actor",
                "Person"
        );
        for(String uri : contextMusic){
            if(uri.contains("http")){
                String uriTemp = "http://dbpedia.org/resource/" + uri;
                if(uriInput.toLowerCase().equals(uriTemp.toLowerCase())){
                    return true;
                }
            }else{
                if(uriInput.toLowerCase().equals(uri.toLowerCase())){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isTopContext(String uriInput){
        List<String> contextMusic = Arrays.asList(
                "Mixed"
        );
        for(String uri : contextMusic){
            if(uri.contains("http")){
                String uriTemp = "http://dbpedia.org/resource/" + uri;
                if(uriInput.toLowerCase().equals(uriTemp.toLowerCase())){
                    return true;
                }
            }else{
                if(uriInput.toLowerCase().equals(uri.toLowerCase())){
                    return true;
                }
            }
        }
        return false;
    }
}
