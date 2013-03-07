#include <jni.h>

class JClass {

  JNIEnv *env;
  jobject &obj;
  jclass cls;
  
public:

  JClass(JNIEnv *env, jobject &obj):
    env(env), obj(obj) { 

    cls = env->GetObjectClass(obj);
  }

  const char * name() {
    jmethodID mid = env->GetMethodID(cls, "getClass", "()Ljava/lang/Class;");
    jobject clsObj = env->CallObjectMethod(obj, mid);

    jclass ccls = env->GetObjectClass(clsObj);
    mid = env->GetMethodID(ccls, "getName", "()Ljava/lang/String;");

    jstring strObj = (jstring)env->CallObjectMethod(clsObj, mid);

    return env->GetStringUTFChars(strObj, NULL);

    //env->ReleaseStringUTFChars(strObj, str);
  }
};
