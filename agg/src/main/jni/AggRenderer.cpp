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

void compop(Style* style, jstring comp_op, JNIEnv *env) {
  const char* c = env->GetStringUTFChars(comp_op, JNI_FALSE);
  std::string str = c;

  style->comp_op = CompOp::map[str];
  env->ReleaseStringUTFChars(comp_op, c); 
}

RenderingPipeline<VertexSource> * get_rp(jlong h) {
  return (RenderingPipeline<VertexSource> *) h;
}

JNIEXPORT jlong JNICALL Java_org_jeo_agg_AggRenderer_createRenderingPipeline
  (JNIEnv *env, jobject obj, jint width, jint height) {

  RenderingPipeline<VertexSource> *rp = 
      new RenderingPipeline<VertexSource>(width, height);
  return (jlong) rp;
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_setTransform
  (JNIEnv *env, jobject obj, jlong rph, jdouble scx, jdouble scy, 
  jdouble tx, jdouble ty) {

  RenderingPipeline<VertexSource> *rp = get_rp(rph); 
  rp->set_transform(scx, scy, tx, ty);
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_setBackground
  (JNIEnv *env, jobject obj, jlong rph, jfloatArray bgcolor) {

  agg::rgba8 bgcol;
  color(&bgcol, bgcolor, env);

  RenderingPipeline<VertexSource> *rp = get_rp(rph); 
  rp->set_background(bgcol);
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_drawLine
  (JNIEnv *env, jobject obj, jlong rph, jobject line, 
  jfloatArray line_color, jfloat width, jbyte join, jbyte cap, 
  jdoubleArray dash, jstring comp_op) {

  LineStyle style;
  color(&style.color, line_color, env);
  style.width = width; 
  style.join = (agg::line_join_e) join;
  style.cap = (agg::line_cap_e) join;

  if (comp_op) {
    compop(&style, comp_op, env);
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
  RenderingPipeline<VertexSource> *rp = get_rp(rph);
  rp->draw_line(source, style);
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_drawPolygon
  (JNIEnv *env, jobject obj, jlong rph, jobject poly, 
  jfloatArray fill_color, jfloatArray line_color, jfloat line_width,
  jstring comp_op) {

  PolyStyle style;
  color(&style.fill_color, fill_color, env);

  style.line.width = line_width;
  color(&style.line.color, line_color, env);

  if (comp_op) {
    compop(&style, comp_op, env);
  }
  VertexSource source(env, poly);
  RenderingPipeline<VertexSource> *rp = get_rp(rph);
  rp->draw_polygon(source, style);
}

JNIEXPORT void JNICALL Java_org_jeo_agg_AggRenderer_writePPM
  (JNIEnv *env, jobject obj, jlong rph, jstring path) {

  RenderingPipeline<VertexSource> *rp = get_rp(rph); 

  const char *filename = env->GetStringUTFChars(path, 0);
  FILE* fd = fopen(filename, "wb");
  if(fd)
  {
    int w = rp->rbuf.width();
    int h = rp->rbuf.height();
        
    fprintf(fd, "P6 %d %d 255 ", w, h);
    fwrite(rp->rbuf.buf(), 1, w * h * rp->depth, fd);
    fclose(fd);
  }

  env->ReleaseStringUTFChars(path, filename);
}

JNIEXPORT jintArray JNICALL Java_org_jeo_agg_AggRenderer_data
  (JNIEnv *env, jobject obj, jlong rph) {

  RenderingPipeline<VertexSource> *rp = get_rp(rph);
  agg::rendering_buffer rbuf = rp->rbuf;

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
