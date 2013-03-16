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

template<class V> RenderingPipeline<V>::RenderingPipeline()
   : at(agg::trans_affine()) {
}

template<class V> void RenderingPipeline<V>::set_transform(
    double scx, double scy, double tx, double ty) {
  at = agg::trans_affine(scx, 0, 0, scy, tx, ty);
}

template<class V> void RenderingPipeline<V>::fill_path(V *path, char *p) {

  while (*p != agg::path_cmd_stop) {
    switch(*p++) {
      case agg::path_cmd_move_to:
        path->move_to(*((float*)p), *((float*)(p+4))); 
        p += 8;
        break;

      case agg::path_cmd_line_to:
        path->line_to(*((float*)p), *((float*)(p+4))); 
        p += 8;
        break;

      case agg::path_cmd_end_poly:
        path->close_polygon(); 
        break;

      case agg::path_cmd_stop:
        return;
    }
  }
}

template<class V> void RenderingPipeline<V>::draw_line(
    V *line, const LineStyle &style, RenderingBuffer *rb) {

  typedef agg::conv_transform<V> LineType;

  // apply transform to line
  LineType tx_line(*line, at);

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

  PixfmtCompType pixf(rb->rbuf);
  pixf.comp_op(style.comp_op);

  agg::renderer_base<PixfmtCompType> renderer_base(pixf);
  agg::renderer_scanline_aa_solid<RendererType> renderer(renderer_base);
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
    V *poly, const PolyStyle &style, RenderingBuffer *rb) {

  typedef agg::rgba8 ColorType;
  typedef agg::order_rgba OrderType;
  typedef agg::comp_op_adaptor_rgba_pre<ColorType, OrderType> BlenderType;
  typedef agg::pixfmt_custom_blend_rgba<BlenderType, agg::rendering_buffer> PixfmtType;
  //typedef agg::pixfmt_rgba32 PixfmtType;
  typedef agg::renderer_base<PixfmtType> RendererType;

  PixfmtType pixf(rb->rbuf);
  pixf.comp_op(style.comp_op);

  agg::renderer_base<PixfmtType> renderer_base(pixf);

  agg::rasterizer_scanline_aa<> rasterizer;
  rasterizer.reset();

  //typedef agg::conv_transform<V> PolyType;
  //PolyType tx_poly(poly, at);
  typedef agg::conv_transform<agg::path_storage> PolyType;
  PolyType tx_poly(*poly, at);

  agg::renderer_scanline_aa_solid<RendererType> renderer(renderer_base);
  agg::scanline_p8 scanline;

  typedef agg::conv_curve<PolyType> CurveType;
  typedef agg::conv_stroke<CurveType> StrokeType;

  CurveType curve(tx_poly);
  StrokeType stroke(curve);

  if (style.fill_color) {
    rasterizer.add_path(curve);
    renderer.color(*style.fill_color);
    agg::render_scanlines(rasterizer, scanline, renderer);
  }

  if (style.line) {
    stroke.width(style.line->width);

    rasterizer.add_path(stroke);
    renderer.color(style.line->color);
    agg::render_scanlines(rasterizer, scanline, renderer);
  }
}

template class RenderingPipeline<agg::path_storage>;
