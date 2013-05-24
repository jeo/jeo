#ifndef _Included_RenderingPipeline
#define _Included_RenderingPipeline

#include "agg_trans_affine.h"
#include "RenderingBuffer.h"
#include "Style.h"

template <class V> class RenderingPipeline {

 public:
    
  RenderingPipeline();

  void set_transform(double scx, double scy, double tx, double ty);

  void fill_path(V *path, char *p);

  void draw_point(V *point, const PointStyle &style, RenderingBuffer *rb);

  void draw_line(V *line, const LineStyle &style, RenderingBuffer *rb);

  void draw_polygon(V *poly, const PolyStyle &style, RenderingBuffer *rb);

 private:

  agg::trans_affine at;

};

#endif
