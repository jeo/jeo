#include "RenderingBuffer.h"

RenderingBuffer::RenderingBuffer(int width, int height, int depth)
    : depth(depth) {

  unsigned char* buffer = new unsigned char[width * height * depth];
  memset(buffer, 0, width * height * depth);
  rbuf.attach(buffer, width, height, width * depth);
}

RenderingBuffer::~RenderingBuffer() {
  delete[] rbuf.buf();
}

void RenderingBuffer::set_background(agg::rgba8 color) {
   unsigned n = rbuf.width() * rbuf.height() * 4;
   unsigned char *p = rbuf.buf();
   for(int i = 0; i < n; i+=4) {
     *p++ = color.r;
     *p++ = color.g; 
     *p++ = color.b; 
     *p++ = color.a; 
   }
}
