#include "Marker.h"

std::map<std::string, agg::marker_e> init_marker_map() {
  std::map<std::string, agg::marker_e> m;

  m["circle"] = agg::marker_circle;
  m["cross"] = agg::marker_cross;
  m["crossed_circle"] = agg::marker_crossed_circle;
  m["dash"] = agg::marker_dash;
  m["diamond"] = agg::marker_diamond;
  m["dot"] = agg::marker_dot;
  m["four_rays"] = agg::marker_four_rays;
  m["pixel"] = agg::marker_pixel;
  m["semiellipse_down"] = agg::marker_semiellipse_down;
  m["semiellipse_left"] = agg::marker_semiellipse_left;
  m["semiellipse_right"] = agg::marker_semiellipse_right;
  m["semiellipse_up"] = agg::marker_semiellipse_up;
  m["square"] = agg::marker_square;
  m["triangle_down"] = agg::marker_triangle_down;
  m["triangle_left"] = agg::marker_triangle_left;
  m["triangle_right"] = agg::marker_triangle_right;
  m["triangle_up"] = agg::marker_triangle_up;
  m["x"] = agg::marker_x;
  return m;
}

std::map<std::string, agg::marker_e> Marker::map = init_marker_map();
