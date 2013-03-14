#include <list>
#include "agg_color_rgba.h"
#include "agg_pixfmt_rgba.h"
#include "agg_math_stroke.h"

enum GammaMethod {
  none, 
  power, 
  linear, 
  threshold, 
  multiply
};

struct Style {
  agg::comp_op_e comp_op;

  Style(): comp_op(agg::comp_op_src) {}
};

struct LineStyle: Style {
  agg::rgba8 color;
  float width;
  agg::line_join_e join;
  agg::line_cap_e cap;
  std::list<double> dash_array;
};

struct PolyStyle: Style {
  agg::rgba8 *fill_color;
  LineStyle *line;

  PolyStyle(): line(0), fill_color(0) {}
};

