package com.example.myprismaapp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private Interpreter tflite;
    private static final String TAG = "MainActivity";
    private List<String> labels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Загрузка модели
            loadModel();

            // Загрузка меток классов
            loadLabels();

            // Загрузка изображений из ресурсов
            Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.gus);
            Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.first);

            // Обработка изображений
            processImage(bitmap1);
            processImage(bitmap2);

            Log.d(TAG, "Обработка завершена успешно.");

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Ошибка при обработке: " + e.getMessage());
        }
    }

    private void loadModel() throws IOException {
        // Загрузка модели из папки assets
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        tflite = new Interpreter(FileUtil.loadMappedFile(this, "model.tflite"), options);

        // Получение информации о входном тензоре
        Tensor inputTensor = tflite.getInputTensor(0);
        int[] inputShape = inputTensor.shape();
        DataType inputDataType = inputTensor.dataType();
        Log.d(TAG, "Входной тензор - shape: " + Arrays.toString(inputShape) + ", data type: " + inputDataType);

        // Получение информации о выходном тензоре
        Tensor outputTensor = tflite.getOutputTensor(0);
        int[] outputShape = outputTensor.shape();
        DataType outputDataType = outputTensor.dataType();
        Log.d(TAG, "Выходной тензор - shape: " + Arrays.toString(outputShape) + ", data type: " + outputDataType);
    }

    private void loadLabels() {
        String fileName = "labels.txt";
        try {
            labels = FileUtil.loadLabels(this, fileName);
            if (labels == null || labels.isEmpty()) {
                Log.e(TAG, "Список меток пустой или не инициализирован.");
            } else {
                Log.d(TAG, "Загружено " + labels.size() + " меток классов.");
            }
        } catch (IOException e) {
            Log.e(TAG, "Ошибка при загрузке файла " + fileName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void processImage(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap равен null. Пропуск обработки.");
            return;
        }

        // Предварительная обработка изображения
        TensorImage inputImage = preprocessImage(bitmap);

        // Запуск модели
        TensorBuffer outputBuffer = runModel(inputImage);

        // Постобработка вывода
        int predictedClass = postprocessOutput(outputBuffer);

        // Действия с предсказанным классом
        if (predictedClass != -1 && predictedClass < labels.size()) {
            String className = labels.get(predictedClass);
            Log.d(TAG, "Предсказанный класс для изображения: " + className + " (индекс: " + predictedClass + ")");
        } else {
            Log.e(TAG, "Некорректный индекс предсказанного класса: " + predictedClass);
        }
    }

    private TensorImage preprocessImage(Bitmap bitmap) {
        // Получение типа данных входного тензора
        DataType imageDataType = tflite.getInputTensor(0).dataType();
        TensorImage tensorImage = new TensorImage(imageDataType);
        tensorImage.load(bitmap);

        // Получение размеров, ожидаемых моделью
        int[] inputShape = tflite.getInputTensor(0).shape();
        int modelInputHeight = inputShape[1];
        int modelInputWidth = inputShape[2];

        Log.d(TAG, "Модель ожидает вход размером: " + Arrays.toString(inputShape));

        // Создание ImageProcessor
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(modelInputHeight, modelInputWidth, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0.0f, 255.0f))
                .build();

        tensorImage = imageProcessor.process(tensorImage);

        return tensorImage;
    }

    private TensorBuffer runModel(TensorImage inputImage) {
        int[] outputShape = tflite.getOutputTensor(0).shape();
        DataType outputDataType = tflite.getOutputTensor(0).dataType();
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType);

        int inputBufferSize = inputImage.getBuffer().capacity();
        int expectedInputBufferSize = tflite.getInputTensor(0).numBytes();

        Log.d(TAG, "Размер входного буфера: " + inputBufferSize + " байт");
        Log.d(TAG, "Ожидаемый размер входного тензора: " + expectedInputBufferSize + " байт");

        if (inputBufferSize != expectedInputBufferSize) {
            Log.e(TAG, "Несоответствие размеров входного буфера и ожидаемого тензора.");
            return null;
        }

        tflite.run(inputImage.getBuffer(), outputBuffer.getBuffer());

        return outputBuffer;
    }

    private int postprocessOutput(TensorBuffer outputBuffer) {
        if (outputBuffer == null) {
            Log.e(TAG, "Выходной буфер равен null. Пропуск постобработки.");
            return -1;
        }

        float[] probabilities = outputBuffer.getFloatArray();

        int maxIndex = -1;
        float maxProb = -Float.MAX_VALUE;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i];
                maxIndex = i;
            }
        }

        Log.d(TAG, "Предсказанный класс: " + maxIndex + ", вероятность: " + maxProb);

        return maxIndex;
    }
}
