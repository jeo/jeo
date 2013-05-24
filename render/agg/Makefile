JAVAH=$(JAVA_HOME)/bin/javah

CLASSES=target/classes
BUILD=target
JNI=src/main/jni
JAV=src/main/java

AGG=/opt/local

CC=g++
CFLAGS=-g -O0 -I$(JAVA_HOME)/include -I$(AGG)/include/agg2
LDFLAGS=-dynamiclib -L$(AGG)/lib -lagg

OBJS=$(BUILD)/AggRenderer.o $(BUILD)/RenderingBuffer.o $(BUILD)/RenderingPipeline.o $(BUILD)/VertexSource.o $(BUILD)/CompOp.o $(BUILD)/Marker.o
HDRS=$(JNI)/AggRenderer.h

TARGET=$(BUILD)/libjeoagg.jnilib

all: $(TARGET)

$(TARGET) : $(OBJS) $(HDRS)
	$(CC) -o $(TARGET) $(OBJS) $(LDFLAGS) 

$(BUILD)/%.o : $(JNI)/%.cpp
	$(CC) -c $(CFLAGS) $< -o $@

$(JNI)/%.h: $(JAV)/org/jeo/agg/%.java
	cd $(CLASSES); $(JAVAH) -o ../../$@ org.jeo.agg.$(basename $(notdir $<))

headers: $(HDRS)

clean:
	rm $(TARGET) $(OBJS)
