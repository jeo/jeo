#include "CompOp.h"
#include "agg_pixfmt_rgba.h"

std::map<std::string, agg::comp_op_e> init_compop_map() {
  std::map<std::string, agg::comp_op_e> m;
  m["clear"] = agg::comp_op_clear;
  m["color-burn"] = agg::comp_op_color_burn;
  m["color-dodge"] = agg::comp_op_color_dodge;
  m["contrast"] = agg::comp_op_contrast;
  m["darken"] = agg::comp_op_darken;
  m["difference"] = agg::comp_op_difference;
  m["dst"] = agg::comp_op_dst;
  m["dst-atop"] = agg::comp_op_dst_atop;
  m["dst-in"] = agg::comp_op_dst_in;
  m["dst-out"] = agg::comp_op_dst_out;
  m["dst-over"] = agg::comp_op_dst_over;
  m["exclusion"] = agg::comp_op_exclusion;
  m["hard-light"] = agg::comp_op_hard_light;
  m["invert"] = agg::comp_op_invert;
  m["invert-rgb"] = agg::comp_op_invert_rgb;
  m["lighten"] = agg::comp_op_lighten;
  m["minus"] = agg::comp_op_minus;
  m["multiply"] = agg::comp_op_multiply;
  m["overlay"] = agg::comp_op_overlay;
  m["plus"] = agg::comp_op_plus;
  m["screen"] = agg::comp_op_screen;
  m["soft-light"] = agg::comp_op_soft_light;
  m["src"] = agg::comp_op_src;
  m["src-atop"] = agg::comp_op_src_atop;
  m["src-in"] = agg::comp_op_src_in;
  m["src-out"] = agg::comp_op_src_out;
  m["src-over"] = agg::comp_op_src_over;
  m["xor"] = agg::comp_op_xor;
  return m;
}

std::map<std::string, agg::comp_op_e> CompOp::map = init_compop_map();
