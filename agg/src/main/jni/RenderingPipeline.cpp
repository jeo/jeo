#include <jni.h>
#include "agg_array.h"
#include "agg_rendering_buffer.h"
#include "agg_pixfmt_rgb.h"
#include "agg_color_rgba.h"
#include "agg_basics.h"
#include "agg_renderer_base.h"
#include "agg_renderer_scanline.h"
#include "agg_rasterizer_scanline_aa.h"
#include "agg_scanline_p.h"
#include "agg_scanline_u.h"
#include "agg_path_storage.h"
#include "agg_conv_curve.h"
#include "agg_conv_stroke.h"
#include "agg_conv_dash.h"
#include "agg_conv_transform.h"
#include "RenderingPipeline.h"
#include "VertexSource.h"

template<class V> RenderingPipeline<V>::RenderingPipeline (
    unsigned w, unsigned h) : depth(4) {

  unsigned char* buffer = new unsigned char[w * h * depth];
  memset(buffer, 255, w * h * depth);

  rbuf.attach(buffer, w, h, w * depth);
  at = agg::trans_affine();
}

template<class V> void RenderingPipeline<V>::set_transform(
    double scx, double scy, double tx, double ty) {
  at = agg::trans_affine(scx, 0, 0, scy, tx, ty);
}

template<class V> void RenderingPipeline<V>::draw_line(
    V line, const LineStyle &style) {

  // create base stroke
  agg::conv_stroke<V> stroke(line); 
  stroke.width(style.width);

  // apply the transform
  agg::conv_transform<agg::conv_stroke<V> > tx_stroke(stroke, at);

  // create the renderer
  typedef agg::rgba8 color_type;
  typedef agg::order_rgba order_type;
  typedef agg::pixel32_type pixel_type;
  typedef agg::comp_op_adaptor_rgba_pre<color_type, order_type> blender_type;
  typedef agg::pixfmt_custom_blend_rgba<blender_type, agg::rendering_buffer> pixfmt_comp_type;
  typedef agg::renderer_base<pixfmt_comp_type> renderer_type;

  pixfmt_comp_type pixf(rbuf);
  pixf.comp_op(style.comp_op);

  agg::renderer_base<pixfmt_comp_type> rb(pixf);
  agg::renderer_scanline_aa_solid<renderer_type> renderer(rb);
  renderer.color(style.color);

  // create the rasterizer
  agg::rasterizer_scanline_aa<> rasterizer;
  rasterizer.reset();

  if (style.dash_array.size() > 0) {
    stroke.width(0);

    typedef agg::conv_transform<agg::conv_stroke<V> > base_stroke;

    //dash the stroke
    agg::conv_dash<base_stroke> dash_stroke(tx_stroke);
    std::list<double>::const_iterator it = style.dash_array.begin();
    while(it != style.dash_array.end()) {
      dash_stroke.add_dash(*it++, *it++);
    }

    agg::conv_stroke<agg::conv_dash<base_stroke> > stroke_dash(dash_stroke);
    stroke_dash.width(style.width);
    rasterizer.add_path(stroke_dash);
  }
  else {
    rasterizer.add_path(tx_stroke);
  }

  agg::scanline_p8 scanline;
  agg::render_scanlines(rasterizer, scanline, renderer);
}

template<class V> void RenderingPipeline<V>::draw_polygon(
    V poly, const PolyStyle &style) {

  typedef agg::rgba8 color_type;
  typedef agg::order_rgba order_type;
  typedef agg::pixel32_type pixel_type;
  typedef agg::comp_op_adaptor_rgba_pre<color_type, order_type> blender_type;
  typedef agg::pixfmt_custom_blend_rgba<blender_type, agg::rendering_buffer> pixfmt_comp_type;
  typedef agg::renderer_base<pixfmt_comp_type> renderer_type;

  pixfmt_comp_type pixf(rbuf);
  pixf.comp_op(style.comp_op);

  agg::renderer_base<pixfmt_comp_type> rb(pixf);

  agg::rasterizer_scanline_aa<> rasterizer;
  rasterizer.reset();

  agg::conv_curve<V> curve(poly);
  agg::conv_transform<agg::conv_curve<V> > tx_curve(curve, at);

  agg::conv_stroke<agg::conv_curve<V> > stroke(curve); 
  stroke.width(style.line.width);
  agg::conv_transform<agg::conv_stroke<agg::conv_curve<V> > > tx_stroke(stroke,at);

  agg::renderer_scanline_aa_solid<renderer_type> renderer(rb);
  agg::scanline_p8 scanline;

  rasterizer.add_path(tx_curve);
  renderer.color(style.fill_color);
  agg::render_scanlines(rasterizer, scanline, renderer);

  rasterizer.add_path(tx_stroke);
  renderer.color(style.line.color);
  agg::render_scanlines(rasterizer, scanline, renderer);
}

template class RenderingPipeline<VertexSource>;
