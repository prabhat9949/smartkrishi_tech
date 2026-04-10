package com.smartkrishi.ml;

import android.content.Context;
import android.graphics.Bitmap;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0015\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J\b\u0010\u0012\u001a\u00020\u0013H\u0016J\u0016\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u0015\u001a\u00020\rH\u0002J\u0010\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0015\u001a\u00020\rH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/smartkrishi/ml/PlantDiseaseClassifier;", "Ljava/lang/AutoCloseable;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "inputImageSize", "", "inputShape", "", "interpreter", "Lorg/tensorflow/lite/Interpreter;", "labels", "", "", "classify", "Lcom/smartkrishi/ml/DiseasePrediction;", "bitmap", "Landroid/graphics/Bitmap;", "close", "", "loadLabels", "fileName", "loadModelFile", "Ljava/nio/MappedByteBuffer;", "app_debug"})
public final class PlantDiseaseClassifier implements java.lang.AutoCloseable {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final org.tensorflow.lite.Interpreter interpreter = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> labels = null;
    @org.jetbrains.annotations.NotNull()
    private final int[] inputShape = null;
    private final int inputImageSize = 0;
    
    public PlantDiseaseClassifier(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final java.nio.MappedByteBuffer loadModelFile(java.lang.String fileName) {
        return null;
    }
    
    private final java.util.List<java.lang.String> loadLabels(java.lang.String fileName) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.smartkrishi.ml.DiseasePrediction classify(@org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap bitmap) {
        return null;
    }
    
    @java.lang.Override()
    public void close() {
    }
}