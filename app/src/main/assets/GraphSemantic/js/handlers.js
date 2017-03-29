const DBPEDIA_URL = "http://dbpedia-test.inria.fr/sparql?default-graph-uri=&query=";
const RESULT_RETURN_TYPE = "&format=application%2Fsparql-results%2Bjson&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000";

var nodes = []
var edges = []
var vertexs = [];

var fromUris = [];
var recommendedUri;

window.addEventListener("load", function () {
    init();
});

function init(){
    recommendedUri = "http://dbpedia.org/resource/Lionel_Jeffries";
    fromUris = ["http://dbpedia.org/resource/Love", "http://dbpedia.org/resource/The_Love_Boat"];

    var data = get_data_post();

        $.ajax({
            url:    "http://semreco.inria.fr/hub/search/graph",
            type:   "POST",
            data:   data,
            dataType: "json",
            success: function(data){
                get_detail_info(data);
            },
            error: function(message){

            }
        });
}

function get_data_post(){
    var uris = fromUris;
    uris.push(recommendedUri);

    var data = $.param({"nodes[]": uris}, true);
    console.log("data post: " + data);
    return data;
}

function get_detail_info(root_data){
    var uris = [];
    for(var i = 0; i < root_data.length; i++){
        var x_uri = root_data[i].x.value;
        var z_uri = root_data[i].z.value;

        if(uris.indexOf(x_uri) == -1){
            uris.push(x_uri);
        }
        if(uris.indexOf(z_uri) == -1){
            uris.push(z_uri);
        }
    }
    console.log(uris);
    var url = create_url_dbpedia(uris);
    console.log(url);
    $.ajax({
        type:       "GET",
        url:        url,
        root_data:  root_data,
        dataType:   "json",
        success:    function(data){
            parse_details_info(this.root_data, data);
        },
        error:      function(message){
            console.log("Error: " + message);
        }
    });
}

function create_url_dbpedia(uris){
    var query = "SELECT DISTINCT ?uri ?image ?label ?description WHERE { ?uri rdfs:label ?label . OPTIONAL {?uri rdfs:comment ?description} . OPTIONAL {?uri <http://dbpedia.org/ontology/thumbnail> ?image} . FILTER (";
    for(var i = 0; i < uris.length; i++){
        if(i == 0){
            query += "?uri=<" + uris[i] + "> ";
        }else{
            query += "|| ?uri=<" + uris[i] + "> ";
        }
    }
    query += ") . FILTER ( lang(?label) = 'en' && lang(?description) = 'en' )}";
    console.log(query);
    var url = DBPEDIA_URL + encodeURIComponent(query) + RESULT_RETURN_TYPE;
    return url;
}

function parse_details_info(root_data, data){
    console.log("root data");
    console.log(root_data);
    if(data.results.bindings != undefined){
        var arr_result = data.results.bindings;
        for(var i = 0; i < arr_result.length; i++){
            var uri = arr_result[i].uri.value;
            var label = arr_result[i].label.value;
            var description = arr_result[i].description.value;
            var image = "";
            if(arr_result[i].image != undefined){
                image = arr_result[i].image.value;
            }else{
                image = "no_image.png";
            }
            update_root_data(root_data, uri, label, description, image);
        }
        draw_graph(root_data);
    }
}

function update_root_data(root_data, uri, label, description, image){
    for(var i = 0; i < root_data.length; i++){
        var x_uri = root_data[i].x.value;
        var z_uri = root_data[i].z.value;
        if(x_uri == uri){
            root_data[i].x.label = label;
            root_data[i].x.description = description;
            root_data[i].x.image = image;
        }
        if(z_uri == uri){
            root_data[i].z.label = label;
            root_data[i].z.description = description;
            root_data[i].z.image = image;
        }
    }
}

function draw_graph(data){
    for(var i = 0; i < data.length; i++){
        var x_uri = data[i].x.value;
        var z_uri = data[i].z.value;
        var is_x = false;
        var is_z = false;

        if(find_vertext_by_uri(x_uri) == null){
            var vertex = {
                        id:             create_id_vertex(),
                        uri:            x_uri,
                        shape:          "circularImage",
                        image:          data[i].x.image,
                        label:          data[i].x.label,
                        description:    data[i].x.description,
                        target_id:      []
                    };
            console.log("vertex id: " + vertex.id);
            vertexs.push(vertex);
        }
        if(find_vertext_by_uri(z_uri) == null){
            var vertex = {
                        id:             create_id_vertex(),
                        uri:            z_uri,
                        shape:          "circularImage",
                        image:          data[i].z.image,
                        label:          data[i].z.label,
                        description:    data[i].z.description,
                        target_id:      []
                    };
            console.log("vertex id: " + vertex.id);
            vertexs.push(vertex);
        }
    }

    for(var i = 0; i < data.length; i++){
        var x_uri = data[i].x.value;
        var z_uri = data[i].z.value;

        var vertex_x = find_vertext_by_uri(x_uri);
        var vertex_z = find_vertext_by_uri(z_uri);
        if(vertex_x != null && vertex_z != null){
            if(vertex_x.target_id.indexOf(vertex_z.id) == -1 && 
                vertex_z.target_id.indexOf(vertex_x.id) == -1){
                vertex_x.target_id.push(vertex_z.id);
            }
        }
    }

    console.log("vertexs");
    console.log(vertexs);

    draw_svg_graph();
}

function find_vertext_by_uri(uri){
    for(var i = 0; i < vertexs.length; i++){
        if(vertexs[i].uri == uri){
            return vertexs[i];
        }
    }
    return null;
}

function find_vertex_by_id(id){
    for(var i = 0; i < vertexs.length; i++){
        if(vertexs[i].id == id){
            return vertexs[i];
        }
    }
    return null;
}

function create_id_vertex(){
    var max_id = vertexs.length;
    var id = max_id + 1;
    return id;
}

function draw_svg_graph(){
    var svg = new SVG(document.querySelector(".graph")).size("100%", 500);
    var links = svg.group();
    var markers = svg.group();
    var nodes = svg.group();

    var g1 = nodes.group().translate(300, 100).draggy();
    g1.circle(80).fill("#C2185B");

    var g2 = nodes.group().translate(100, 100).draggy();
    g2.circle(50).fill("#E91E63");

    var g3 = nodes.group().translate(200, 300).draggy();
    g3.circle(100).fill("#FF5252");

    var g4 = nodes.group().translate(100, 100).draggy();
    g4.circle(50).fill("#E91E63");

    var g5 = nodes.group().translate(100, 100).draggy();
    g5.circle(50).fill("#FF5252");

    var g6 = nodes.group().translate(100, 100).draggy();
    g6.circle(100).fill("#FF5252");

    var g7 = nodes.group().translate(100, 100).draggy();
    g7.circle(100).fill("#FF5252");

    var g8 = nodes.group().translate(100, 100).draggy();
    g8.circle(100).fill("#FF5252");

    g1.connectable({
        container: links,
        markers: markers
    }, g2).setLineColor("#5D4037");

    g2.connectable({
        container: links,
        markers: markers
    }, g1).setLineColor("#5D4037");


    g2.connectable({
        container: links,
        markers: markers
    }, g3).setLineColor("#5D4037")

    g3.connectable({
        container: links,
        markers: markers
    }, g2).setLineColor("#5D4037")

    g1.connectable({
        container: links,
        markers: markers
    }, g4).setLineColor("#5D4037");

    g1.connectable({
        container: links,
        markers: markers
    }, g5).setLineColor("#5D4037");

    g1.connectable({
        container: links,
        markers: markers
    }, g6).setLineColor("#5D4037");

    g1.connectable({
        container: links,
        markers: markers
    }, g7).setLineColor("#5D4037");

    g1.connectable({
        container: links,
        markers: markers
    }, g8).setLineColor("#5D4037");

    g2.connectable({
        container: links,
        markers: markers
    }, g5).setLineColor("#5D4037");

    g2.connectable({
        container: links,
        markers: markers
    }, g6).setLineColor("#5D4037");

    g2.connectable({
        container: links,
        markers: markers
    }, g7).setLineColor("#5D4037");

    g.connectable({
        container: links,
        markers: markers
    }, g8).setLineColor("#5D4037");
}
