function() {
  if (this.geometry == null) return;

  var coords = this.geometry.coordinates;
  if (coords == null || coords.length == 0) return;

  var x = function(c) { return c[0]; }
  var y = function(c) { return c[1]; }

  var min = function(arr, map) {
    return Math.min.apply(Math, arr.map(map));
  }
  var max = function(arr, map) {
    return Math.max.apply(Math, arr.map(map));
  }

  var bbox = function(arr) {
     return [min(arr,x), min(arr,y), max(arr,x), max(arr,y)];
  } 

  var union = function(b1, b2) {
    if (b1 == null) return b2;
    return [Math.min(b1[0], b2[0]), Math.min(b1[1], b2[1]),
            Math.max(b1[2], b2[2]), Math.max(b1[3], b2[3])];
  }

  var type = this.geometry.type;
  var box = null;

  if (type == "Point") {
     var c = coords;
     if (c != null && c.length > 0) {
       box = [c[0],c[1],c[0],c[1]];
     }
   }
   else if (type == "LineString") {
     var cc = coords;
     box = bbox(cc);
   }
   else if (type == "Polygon") {
     var cc = coords[0];
     box = bbox(cc);
   }
   else if (type == "MultiPoint") {
     var cc = coords;
     box = bbox(cc);
   }
   else if (type == "MultiLineString") {
     var c3 = coords;
     for (var i = 0; i < c3.length; i++) {
       box = union(box, bbox(c3[i]));
     }
   }
   else if (type == "MultiPolygon") {
     var c4 = coords;
     for (var c3 in c4) {
       box = union(box, bbox(c3[0]));
     }
   }
   else if (type == "GeometryCollection") {
   }

   emit("bbox", box);
}