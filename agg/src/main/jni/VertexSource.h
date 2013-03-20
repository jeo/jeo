#include <jni.h>

class VertexSource {

 JNIEnv *env;
 jobject &obj;
 jdoubleArray xy;
 jmethodID rewind_mid;
 jmethodID vertex_mid;

public:

 VertexSource(JNIEnv *env, jobject &obj);

 void rewind(unsigned path_id);

 unsigned vertex(double* x, double* y);

};
