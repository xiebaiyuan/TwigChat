#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_twigchat_xiebaiyuan_twigchat_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "I love aixia";
    return env->NewStringUTF(hello.c_str());
}
