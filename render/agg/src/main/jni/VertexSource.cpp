#include <jni.h>
#include "agg_basics.h"
#include "VertexSource.h"

VertexSource::VertexSource(JNIEnv *env, jobject &obj):
  env(env), obj(obj), xy(env->NewDoubleArray(2)) { 

  jclass cls = env->GetObjectClass(obj);
  vertex_mid = env->GetMethodID(cls, "vertex", "([D)B");
  rewind_mid = env->GetMethodID(cls, "rewind", "(I)V");
}

void VertexSource::rewind(unsigned path_id) {
  env->CallVoidMethod(obj, rewind_mid, path_id);
}

unsigned VertexSource::vertex(double* x, double* y) {
  jint cmd = env->CallIntMethod(obj, vertex_mid, xy);
  if (cmd == agg::path_cmd_stop) {
    return (unsigned) cmd;
  }

  double *d = env->GetDoubleArrayElements(xy, JNI_FALSE);

  *x = *d;
  *y = *(d+1);

  env->ReleaseDoubleArrayElements(xy, d, 0);
  return cmd;
}
