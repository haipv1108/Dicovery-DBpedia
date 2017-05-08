const DBPEDIA_URL = "http://dbpedia-test.inria.fr/sparql?default-graph-uri=&query=";
const RESULT_RETURN_TYPE = "&format=application%2Fsparql-results%2Bjson&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000";

var vertexs = [];
var fromUris = [];
var nodeDataArray = [];
var linkDataArray = [];
var recommendedUri;
var propertyMap = [];

window.onload = function(e){
  init();
}

function init(){
  // recommendedUri = "http://dbpedia.org/resource/Lionel_Jeffries";
  // fromUris = ["http://dbpedia.org/resource/Love", "http://dbpedia.org/resource/The_Love_Boat"];
  
  // recommendedUri = "http://dbpedia.org/resource/Tom_Hanks";
  // fromUris = ["http://dbpedia.org/resource/Leonardo_DiCaprio"];

  // get_graph_data();

  recommendedUri = JSInterface.getRecommendedUri();
  var length = JSInterface.getLengthFromUris();
  for(var i = 0; i < length; i++){
      var uri = JSInterface.getValueOfFromUris(i);
      fromUris.push(uri);
  }
  JSInterface.showToast(fromUris.length);

  JSInterface.showProgressDialog();
  get_graph_data();
}

function get_graph_data(){
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
    var query = "SELECT DISTINCT ?uri ?label ?description WHERE { ?uri rdfs:label ?label . OPTIONAL {?uri rdfs:comment ?description} . FILTER (";
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
            update_root_data(root_data, uri, label, description);
        }
        create_graph_details(root_data);
    }
}

function update_root_data(root_data, uri, label, description){
    for(var i = 0; i < root_data.length; i++){
        var x_uri = root_data[i].x.value;
        var z_uri = root_data[i].z.value;
        if(x_uri == uri){
            root_data[i].x.label = label;
            root_data[i].x.description = description;
        }
        if(z_uri == uri){
            root_data[i].z.label = label;
            root_data[i].z.description = description;
        }
    }
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

function get_label_from_uri(uri){
    var split = uri.split("/");
    return split[split.length - 1];
}

function find_key_property(uri){
    for(var i = 0; i < propertyMap.length; i++){
      if(propertyMap[i].uri == uri){
        return propertyMap[i].key;
      }
    }
    propertyMap.push({
      key:  propertyMap.length + 1,
      uri:  uri,
      label:  get_label_from_uri(uri)
    });
    return propertyMap[propertyMap.length - 1].key;
}

function create_graph_details(root_data){
  for(var i = 0; i < root_data.length; i++){
    var x_uri = root_data[i].x.value;
    var z_uri = root_data[i].z.value;
    var is_x = false;
    var is_z = false;

    if(find_vertext_by_uri(x_uri) == null){
        var vertex = {
                    id:             create_id_vertex(),
                    uri:            x_uri,
                    label:          root_data[i].x.label,
                    description:    root_data[i].x.description,
                    target_id:      []
                };
        console.log("vertex id: " + vertex.id);
        vertexs.push(vertex);
    }
    if(find_vertext_by_uri(z_uri) == null){
        var vertex = {
                    id:             create_id_vertex(),
                    uri:            z_uri,
                    label:          root_data[i].z.label,
                    description:    root_data[i].z.description,
                    target_id:      []
                };
        console.log("vertex id: " + vertex.id);
        vertexs.push(vertex);
    }
  }

  for(var i = 0; i < root_data.length; i++){
      var x_uri = root_data[i].x.value;
      var z_uri = root_data[i].z.value;
      var property_key = find_key_property(root_data[i].y.value);

      var vertex_x = find_vertext_by_uri(x_uri);
      var vertex_z = find_vertext_by_uri(z_uri);
      if(vertex_x != null && vertex_z != null){
          if(vertex_x.target_id.indexOf(vertex_z.id) == -1){
              vertex_x.target_id.push({
                id: vertex_z.id,
                property_key: property_key
              });
          }
      }
  }

  console.log("vertexs");
  console.log(vertexs);

  JSInterface.dismissProgressDialog();

  start_graph();
}