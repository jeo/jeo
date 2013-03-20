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
#include "Marker.h"

typedef RenderingPipeline<agg::path_storage> RenderingPipelineType;

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

agg::marker_e marker(jstring marker, JNIEnv *env) {
  agg::marker_e m = Marker::map["circle"];
  if (marker) {
    const char* c = env->GetStringUTFChars(marker, JNI_FALSE);
    std::string str = c;

    if (Marker::map.count(str) > 0) {
      m = Marker::map[str];
    }

    env->ReleaseStringUTFChars(marker, c);
  }
  return m;
}

void set_gamma_method(jstring gamma_method, std::string *str, JNIEnv *env) {
  if (gamma_method) {
    const char *c = env->GetStringUTFChars(gamma_method, JNI_FALSE);

    *str = c;

    env->ReleaseStringUTFChars(gamma_method, c);
  }
}

RenderingPipelineType * get_rp(jlong h) {
  return (RenderingPipelineType *) h;
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

  return (jlong) new RenderingPipelineType();
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_setTransform
  (JNIEnv *env, jobject obj, jlong rph, jdouble scx, jdouble scy, 
  jdouble tx, jdouble ty) {

  RenderingPipelineType *rp = get_rp(rph); 
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

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_disposeBuffer
  (JNIEnv *env, jobject obj, jlong rbh) {
  
  RenderingBuffer *dst = get_rb(rbh);
  delete dst;
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_disposePipeline
  (JNIEnv *env, jobject obj, jlong rph) {

  RenderingPipelineType *rp = get_rp(rph);
  delete rp;
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_drawPoint
  (JNIEnv *env, jobject obj, jlong rph, jlong rbh, jobject point, 
   jstring shape, jfloatArray fill_color, jfloat width, jfloat height, 
   jfloatArray line_color, jstring comp_op) {

  PointStyle style;
  color(&style.color, fill_color, env);
  style.width = width;
  style.height = height;
  style.marker = marker(shape, env);
   
  if (line_color) {
    LineStyle line_style; 
    color(&line_style.color, line_color, env);
    style.line = &line_style;
  }

  char *p = (char *)env->GetDirectBufferAddress(point);

  agg::path_storage path;

  RenderingBuffer *rb = get_rb(rbh);
  RenderingPipelineType *rp = get_rp(rph);

  rp->fill_path(&path, p);
  rp->draw_point(&path, style, rb);
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_drawLine
  (JNIEnv *env, jobject obj, jlong rph, jlong rbh, jobject line, 
  jfloatArray line_color, jfloat width, jbyte join, jbyte cap, 
  jdoubleArray dash, jfloat gamma, jstring gamma_method, jstring comp_op) {

  LineStyle style;
  color(&style.color, line_color, env);
  style.width = width; 
  style.join = (agg::line_join_e) join;
  style.cap = (agg::line_cap_e) join;
  style.gamma = gamma;
  set_gamma_method(gamma_method, &style.gamma_method, env);

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

  char *p = (char *)env->GetDirectBufferAddress(line);
  agg::path_storage path;

  RenderingBuffer *rb = get_rb(rbh);
  RenderingPipelineType *rp = get_rp(rph);

  rp->fill_path(&path, p);
  rp->draw_line(&path, style, rb);
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_drawPolygon
  (JNIEnv *env, jobject obj, jlong rph, jlong rbh, jobject poly, 
  jfloatArray fill_color, jfloat gamma, jstring gamma_method, 
  jfloatArray line_color, jfloat line_width, jfloat line_gamma, 
  jstring line_gamma_method, jstring comp_op) {

  PolyStyle style;
  if (fill_color) {
    agg::rgba8 col; 
    color(&col, fill_color, env);
    style.fill_color = &col;
    style.gamma = gamma;
    set_gamma_method(gamma_method, &style.gamma_method, env);
  }
  //color(&style.fill_color, fill_color, env);

  if (line_color) {
    LineStyle line_style; 
    color(&line_style.color, line_color, env);
    line_style.width = line_width;
    style.line = &line_style;
    style.gamma = line_gamma;
    set_gamma_method(line_gamma_method, &style.gamma_method, env);
  }

  if (comp_op) {
    style.comp_op = compop(comp_op, env);
  }

  char *p = (char *)env->GetDirectBufferAddress(poly);
  agg::path_storage path;

  RenderingBuffer *rb = get_rb(rbh);
  RenderingPipelineType *rp = get_rp(rph);

  rp->fill_path(&path, p);
  rp->draw_polygon(&path, style, rb);
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
