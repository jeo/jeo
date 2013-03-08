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

  Style(): comp_op(agg::comp_op_src_over) {
  }
};

struct LineStyle: Style {
  agg::rgba8 color;
  float width;
  agg::line_join_e join;
  agg::line_cap_e cap;
  std::list<double> dash_array;
  float gamma;   
  GammaMethod gamma_method;
};

struct PolyStyle: Style {
  agg::rgba8 fill_color;

  float gamma;   
  GammaMethod gamma_method;

  LineStyle line;
};

