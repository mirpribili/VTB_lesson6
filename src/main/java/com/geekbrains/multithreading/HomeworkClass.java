package com.geekbrains.multithreading;

import java.util.Arrays;

public class HomeworkClass {
    /*
1.
    Необходимо написать два метода, которые делают следующее:
*/
    //
    /*
            1) Создают одномерный длинный массив, например:
    static final int SIZE = 10 000 000 ;
    static final int HALF = size / 2 ;
    float [] arr = new float [ size ].
            2) Заполняют этот массив единицами.
            3) Засекают время выполнения: long a = System.currentTimeMillis().
            4) Проходят по всему массиву и для каждой ячейки считают новое значение по формуле:
    arr [ i ] = ( float )( arr [ i ] * Math . sin ( 0.2f + i / 5 ) * Math . cos ( 0.2f + i / 5 ) *
            Math . cos ( 0.4f + i / 2 )).
            5) Проверяется время окончания метода System.currentTimeMillis().
            6) В консоль выводится время работы: System.out.println(System.currentTimeMillis() - a).
    Отличие первого метода от второго:
            ●
    Первый просто бежит по массиву и вычисляет значения.
            ●
    Второй разбивает массив на два массива, в двух потоках высчитывает новые значения и
    потом склеивает эти массивы обратно в один.
    Пример деления одного массива на два:
            ●
            System.arraycopy(arr, 0, a1, 0, h);
            ●
            System.arraycopy(arr, h, a2, 0, h).
    Пример обратной склейки:
            ●
            System.arraycopy(a1, 0, arr, 0, h);
            ●
        System.arraycopy(a2, 0, arr, h, h).
    Примечание:
            System.arraycopy() — копирует данные из одного массива в другой:
            System.arraycopy(массив-источник, откуда начинаем брать данные из массива-источника,
            массив-назначение, откуда начинаем записывать данные в массив-назначение, сколько ячеек
            копируем)

     */
    public static void main(String[] args) {
        //oneThreadTask();
        twoThreadTask();
    }
    public static void oneThreadTask(){
        final int ARRAY_LENGTH = 4_000_000;
        float[] arr = new float[ARRAY_LENGTH];
        Arrays.fill(arr, 1.0f);
        long time = System.currentTimeMillis();
        for(int i = 0; i < arr.length; i++){
            arr[i] = (float) (arr[i] * Math.sin(0.2f + i /5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
        }
        System.out.println("time: " + (System.currentTimeMillis() - time));
    }

    public static void twoThreadTask(){
        final int ARRAY_LENGTH = 4_000_000;
        final int HALF_ARRAY_LENGTH = ARRAY_LENGTH/2;
        float[] arr = new float[ARRAY_LENGTH];
        Arrays.fill(arr, 1.0f);
        long time = System.currentTimeMillis();
        float[] leftArray = new float[HALF_ARRAY_LENGTH];
        float[] rightArray = new float[HALF_ARRAY_LENGTH];
        System.arraycopy(arr, 0, leftArray, 0, HALF_ARRAY_LENGTH);
        System.arraycopy(arr, HALF_ARRAY_LENGTH, rightArray, 0, HALF_ARRAY_LENGTH);
// _ WARNING there are we get different results
        Thread threadLeft = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < leftArray.length; i++){
                    leftArray[i] = (float) (leftArray[i] * Math.sin(0.2f + i /5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
                }
            }
        });
        Thread threadRight = new Thread(() -> {
            // ERROR
            // for(int i = 0; i < rightArray.length; i++){
            for(int i = 0, j = HALF_ARRAY_LENGTH; i < rightArray.length; i++, j++){
                //ERROR
                // rightArray[i] = (float) (rightArray[i] * Math.sin(0.2f + i /5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
                rightArray[i] = (float) (rightArray[i] * Math.sin(0.2f + j /5) * Math.cos(0.2f + j / 5) * Math.cos(0.4f + j / 2));
            }
        });
// ^ WARNING there are we get different results

        threadLeft.start();
        threadRight.start();
        try{
            threadLeft.join();
            threadRight.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }


        System.arraycopy(leftArray, 0, arr, 0, HALF_ARRAY_LENGTH);
        System.arraycopy(rightArray, 0, arr, HALF_ARRAY_LENGTH, HALF_ARRAY_LENGTH);
        System.out.println("time: " + (System.currentTimeMillis() - time));
    }
}
