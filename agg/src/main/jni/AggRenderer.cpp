#include <iostream>
#include <string.h>
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
#include "agg_path_storage.h"
#include "agg_conv_curve.h"
#include "agg_conv_stroke.h"
#include "AggRenderer.h"
#include "RenderingPipeline.h"
#include "VertexSource.h"
#include "CompOp.h"

void color(agg::rgba8 *rgb, const jfloatArray arr, JNIEnv *env) {
  jfloat *f = env->GetFloatArrayElements(arr, JNI_FALSE);
  rgb->r = *f;
  rgb->g = *(f+1);
  rgb->b = *(f+2);
  rgb->a = *(f+3);

  env->ReleaseFloatArrayElements(arr, f, 0);
}

agg::comp_op_e compop(jstring comp_op, JNIEnv *env) {
  agg::comp_op_e op = CompOp::map["src"];
  if (comp_op) {
    const char* c = env->GetStringUTFChars(comp_op, JNI_FALSE);
    std::string str = c;
  
    if (CompOp::map.count(str) > 0) {
      op = CompOp::map[str];
    }

    env->ReleaseStringUTFChars(comp_op, c); 
  }

  return op; 
}

RenderingPipeline<VertexSource> * get_rp(jlong h) {
  return (RenderingPipeline<VertexSource> *) h;
}

RenderingBuffer * get_rb(jlong h) {
  return (RenderingBuffer *) h;
}

JNIEXPORT jlong JNICALL Java_org_jeo_agg_AggRenderer_createRenderingBuffer
  (JNIEnv *env, jobject obj, jint width, jint height) {
  return (jlong) new RenderingBuffer(width, height, 4); 
}

JNIEXPORT jlong JNICALL Java_org_jeo_agg_AggRenderer_createRenderingPipeline
  (JNIEnv *env, jobject obj) {

  return (jlong) new RenderingPipeline<VertexSource>();
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_setTransform
  (JNIEnv *env, jobject obj, jlong rph, jdouble scx, jdouble scy, 
  jdouble tx, jdouble ty) {

  RenderingPipeline<VertexSource> *rp = get_rp(rph); 
  rp->set_transform(scx, scy, tx, ty);
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_setBackground
  (JNIEnv *env, jobject obj, jlong rbh, jfloatArray bgcolor) {

  agg::rgba8 bgcol;
  color(&bgcol, bgcolor, env);

  RenderingBuffer *rb = get_rb(rbh); 
  rb->set_background(bgcol);
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_composite
  (JNIEnv *env, jobject obj, jlong dst_rbh, jlong src_rbh, jstring comp_op) {

  float opacity = 1.0;
  
  RenderingBuffer *dst = get_rb(dst_rbh);
  RenderingBuffer *src = get_rb(src_rbh);

  typedef agg::comp_op_adaptor_rgba_pre<agg::rgba8, agg::order_rgba> BlenderType;
  typedef agg::pixfmt_custom_blend_rgba<BlenderType, agg::rendering_buffer> PixfmtType;
  typedef agg::renderer_base<PixfmtType> RendererType;

  PixfmtType pixf(dst->rbuf);
  pixf.comp_op(compop(comp_op, env));
    
  agg::pixfmt_rgba32 pixf_mask(src->rbuf);
  //if (premultiply_src)  pixf_mask.premultiply();

  RendererType ren(pixf);
  ren.blend_from(pixf_mask,0,0,0,unsigned(255*opacity));
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_dispose
  (JNIEnv *env, jobject obj, jlong rbh) {
  
  RenderingBuffer *dst = get_rb(rbh);
  delete dst;
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_drawLine
  (JNIEnv *env, jobject obj, jlong rph, jlong rbh, jobject line, 
  jfloatArray line_color, jfloat width, jbyte join, jbyte cap, 
  jdoubleArray dash, jstring comp_op) {

  LineStyle style;
  color(&style.color, line_color, env);
  style.width = width; 
  style.join = (agg::line_join_e) join;
  style.cap = (agg::line_cap_e) join;

  if (comp_op) {
    style.comp_op = compop(comp_op, env);
  }

  if (dash) {
    jdouble *d = env->GetDoubleArrayElements(dash, JNI_FALSE);
    jsize n = env->GetArrayLength(dash);

    for (int i = 0; i < n; i++) {
      style.dash_array.push_back(*(d+i));
    }
    env->ReleaseDoubleArrayElements(dash, d, 0);
  }

  VertexSource source(env, line);

  RenderingBuffer *rb = get_rb(rbh);
  RenderingPipeline<VertexSource> *rp = get_rp(rph);
  rp->draw_line(source, style, rb);
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_drawPolygon
  (JNIEnv *env, jobject obj, jlong rph, jlong rbh, jobject poly, 
  jfloatArray fill_color, jfloatArray line_color, jfloat line_width,
  jstring comp_op) {

  PolyStyle style;
  if (fill_color) {
    agg::rgba8 col; 
    color(&col, fill_color, env);
    style.fill_color = &col;
  }
  //color(&style.fill_color, fill_color, env);

  if (line_color) {
    LineStyle line_style; 
    color(&line_style.color, line_color, env);
    line_style.width = line_width;
    style.line = &line_style;
  }

  if (comp_op) {
    style.comp_op = compop(comp_op, env);
  }

  VertexSource source(env, poly);

  RenderingBuffer *rb = get_rb(rbh);
  RenderingPipeline<VertexSource> *rp = get_rp(rph);
  rp->draw_polygon(source, style, rb);
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_writePPM
  (JNIEnv *env, jobject obj, jlong rbh, jstring path) {

  RenderingBuffer *rb = get_rb(rbh); 

  const char *filename = env->GetStringUTFChars(path, 0);
  FILE* fd = fopen(filename, "wb");
  if(fd)
  {
    int w = rb->rbuf.width();
    int h = rb->rbuf.height();
        
    fprintf(fd, "P6 %d %d 255 ", w, h);
    fwrite(rb->rbuf.buf(), 1, w * h * rb->depth, fd);
    fclose(fd);
  }

  env->ReleaseStringUTFChars(path, filename);
}

JNIEXPORT jintArray JNICALL Java_org_jeo_agg_AggRenderer_data
  (JNIEnv *env, jobject obj, jlong rbh) {

  RenderingBuffer *rb = get_rb(rbh);
  agg::rendering_buffer rbuf = rb->rbuf;

  int size = rbuf.height()*rbuf.width();
  int fill[size];

  unsigned char *b = rbuf.buf();
  int buf_size = rbuf.height()*rbuf.stride_abs();
  for (int i = 0; i < size; i++) {
    int x = *b++;
    x = x << 8 | *b++;
    x = x << 8 | *b++;
    x = x << 8 | *b++;
    fill[i] = x;
  }

  jintArray data = env->NewIntArray(size);
  env->SetIntArrayRegion(data, 0, size, fill);
  return data;
}
