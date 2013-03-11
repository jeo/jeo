#ifndef _Included_org_jeo_agg_RenderingPipeline
#define _Included_org_jeo_agg_RenderingPipeline

#include "agg_rendering_buffer.h"
#include "agg_color_rgba.h"
#include "agg_trans_affine.h"
#include "Style.h"

template <class V> class RenderingPipeline {

  agg::trans_affine at;

 public:
    
  int depth;
  agg::rendering_buffer rbuf;

  RenderingPipeline(unsigned width, unsigned height);

  void set_transform(double scx, double scy, double tx, double ty);

  void set_background(agg::rgba8 color);

  void draw_line(V line, const LineStyle &style);

  void draw_polygon(V poly, const PolyStyle &style);

};

#endif
