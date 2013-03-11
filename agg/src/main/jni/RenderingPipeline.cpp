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
  //memset(buffer, 255, w * h * depth);

  rbuf.attach(buffer, w, h, w * depth);
  at = agg::trans_affine();
}

template<class V> void RenderingPipeline<V>::set_transform(
    double scx, double scy, double tx, double ty) {
  at = agg::trans_affine(scx, 0, 0, scy, tx, ty);
}

template<class V> void RenderingPipeline<V>::set_background(agg::rgba8 color) {
   unsigned n = rbuf.width() * rbuf.height() * 4;
   unsigned char *p = rbuf.buf();
   for(int i = 0; i < n; i+=4) {
     *p++ = color.r;
     *p++ = color.g; 
     *p++ = color.b; 
     *p++ = color.a; 
   }
}

template<class V> void RenderingPipeline<V>::draw_line(
    V line, const LineStyle &style) {

  typedef agg::conv_transform<V> LineType;

  // apply transform to line
  LineType tx_line(line, at);

  // create base stroke
  typedef agg::conv_stroke<LineType> StrokeType;
  StrokeType stroke(tx_line);
  stroke.width(style.width);

  // create the renderer
  typedef agg::rgba8 ColorType;
  typedef agg::order_rgba OrderType;
  typedef agg::pixel32_type PixelType;
  typedef agg::comp_op_adaptor_rgba_pre<ColorType, OrderType> BlenderType;
  typedef agg::pixfmt_custom_blend_rgba<BlenderType, agg::rendering_buffer> PixfmtCompType;
  typedef agg::renderer_base<PixfmtCompType> RendererType;

  PixfmtCompType pixf(rbuf);
  pixf.comp_op(style.comp_op);

  agg::renderer_base<PixfmtCompType> rb(pixf);
  agg::renderer_scanline_aa_solid<RendererType> renderer(rb);
  renderer.color(style.color);

  // create the rasterizer
  agg::rasterizer_scanline_aa<> rasterizer;
  rasterizer.reset();

  if (style.dash_array.size() > 0) {
    stroke.width(0);

    //dash the stroke
    typedef agg::conv_dash<StrokeType> DashType;
    DashType dash_stroke(stroke);

    std::list<double>::const_iterator it = style.dash_array.begin();
    while(it != style.dash_array.end()) {
      dash_stroke.add_dash(*it++, *it++);
    }

    //stroke the dash
    agg::conv_stroke<DashType> stroke_dash(dash_stroke);
    stroke_dash.width(style.width);
    rasterizer.add_path(stroke_dash);
  }
  else {
    rasterizer.add_path(stroke);
  }

  agg::scanline_p8 scanline;
  agg::render_scanlines(rasterizer, scanline, renderer);
}

template<class V> void RenderingPipeline<V>::draw_polygon(
    V poly, const PolyStyle &style) {

  typedef agg::rgba8 ColorType;
  typedef agg::order_rgba OrderType;
  typedef agg::comp_op_adaptor_rgba_pre<ColorType, OrderType> BlenderType;
  typedef agg::pixfmt_custom_blend_rgba<BlenderType, agg::rendering_buffer> PixfmtCompType;
  typedef agg::renderer_base<PixfmtCompType> RendererType;

  PixfmtCompType pixf(rbuf);
  pixf.comp_op(style.comp_op);

  agg::renderer_base<PixfmtCompType> rb(pixf);

  agg::rasterizer_scanline_aa<> rasterizer;
  rasterizer.reset();

  typedef agg::conv_transform<V> PolyType;
  PolyType tx_poly(poly, at);

  typedef agg::conv_curve<PolyType> CurveType;
  CurveType curve(tx_poly);

  typedef agg::conv_stroke<CurveType> StrokeType;
  StrokeType stroke(curve);
  stroke.width(style.line.width);

  agg::renderer_scanline_aa_solid<RendererType> renderer(rb);
  agg::scanline_p8 scanline;

  rasterizer.add_path(curve);
  renderer.color(style.fill_color);
  agg::render_scanlines(rasterizer, scanline, renderer);

  rasterizer.add_path(stroke);
  renderer.color(style.line.color);
  agg::render_scanlines(rasterizer, scanline, renderer);
}

template class RenderingPipeline<VertexSource>;
