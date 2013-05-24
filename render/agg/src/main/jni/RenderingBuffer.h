#ifndef _Included_RenderingBuffer
#define _Included_RenderingBuffer

#include "agg_color_rgba.h"
#include "agg_rendering_buffer.h"

class RenderingBuffer {

 public:

  const int depth;
  agg::rendering_buffer rbuf;

  RenderingBuffer(int width, int height, int depth);
  ~RenderingBuffer();

  void set_background(agg::rgba8 color);
};

#endif
