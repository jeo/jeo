function(a, boxes) {
  var x1 = Math.min.apply(Math, boxes.map(function(box){ return box[0]; }));
  var y1 = Math.min.apply(Math, boxes.map(function(box){ return box[1]; }));
  var x2 = Math.max.apply(Math, boxes.map(function(box){ return box[2]; }));
  var y2 = Math.max.apply(Math, boxes.map(function(box){ return box[3]; }));

  return {x1:x1, y1:y1, x2:x2, y2:y2};
}