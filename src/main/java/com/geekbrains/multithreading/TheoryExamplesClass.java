package com.geekbrains.multithreading;
/*
//
    1. implements Runnable VS extend Thread = 1й предпочтительнее, 2й для тонкой настройки тк мы не можем еще от чего-то наследоваться
    2. .start() VS .run() =  папалельный тред основному ИЛИ запуск в основном треде(не паралельно)
 */

//  3. как выполнить код после потока?
public class TheoryExamplesClass {
    public static void main(String[] args) {
        Thread t = new Thread(new Runnable(){
            @Override
            public void run(){
                for (int i = 0; i < 5; i++){
                    System.out.println(i);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
        try {
            t.join(); // заставляем поток подождать окончания выполнения
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("END");
    }
//  4. Как прервать созданые поток вместе с основным - поток даемон работает пока работает хотябы один обычны поток приложения
//  у порожденых даемонов нет привязки к их потоку "родителю" тольк к обычному потоку приложения
    public static class DaemonExample{
        public static void main(String[] args){
            Thread tTimer = new Thread(() -> {
                int time = 0;
                while (true){
                    try {
                        Thread.sleep(250);
                        time++;
                        System.out.println("timer: " + time);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            });
            tTimer.setDaemon(true); // объявляем задачу не обязательной и она завершится вместе с основным потоком
            tTimer.start();
            System.out.println("main -> sleep");
            try {
                Thread.sleep(2000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("main -> end");
        }
    }
    /*
//  методы
        tTimer.getState(); = можно получить состояние потока НО моментально устаревает (запускается, ожидает, завершил работу...)
        isAlive();
        join() ждем когда завершит тред работу  МОЖНО ограничить время ожидания
        setPriority() устанавливаем приоритет но это необязательный параметр для ОС
        getName()
        getId()
        getStackTrace() у каждого потока свой стек трейс и так можно получить
//  КАК ОСТАНОВИТЬ ПОТОК?
        stop() - плохая практика тк моментально останавливает поток но если тот работал с данными то они останутся в памяти в мусорном состоянии
        interrupt() - останавливает но если в потоке есть проверка состояния внутр поля isInterrupted
            Thread.currentThread().isInterrupted() см ниже ThreadStopApp
*/
    public static class ThreadStopApp{
        public static void main(String[] args){
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean inter = false;
                    while (true){
                        if(Thread.currentThread().isInterrupted() || inter){
                            break;
                        }
                        System.out.println("tick");
                        try {
                            Thread.sleep(550);
                        }catch (InterruptedException e){
                            inter = true;
                        }
                    }
                }
            });
            t.start();
            try {
                Thread.sleep(3000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            t.interrupt();
        }
    }
/*          создаем условие в безопасном месте для остановки потока
            InterruptedException нам тут помогает
// Как быть если 2 праралельных треда обращаются к 1 классу напр. счетчику?
        Использовать public synchronized void dec() \ inc() {} #6-51:12
            incThread.join();
            тут ждем и не идем дальше((
            decThread.join();
// СИНХРОНИЗАЦИЯ
        Способ 1
            synchronized = public synchronized void dec()
*       Способ 2
*/
    public static class SyncMethodsApp {
        public static void main(String[] args){
            SyncMethodsApp e1 = new SyncMethodsApp();
            SyncMethodsApp e2 = new SyncMethodsApp();
            new Thread(() -> e1.method1()).start(); // стартовать может 1м любой из методов (1 и 2)
            new Thread(() -> e1.method2()).start(); // тут e1 захваченный монитор и поэтому методы будут вызванны последовательно
        }
        public synchronized void method1(){
            System.out.println("M1-Start");
            for (int i=0; i<10; i++){
                try {
                    Thread.sleep(10);
                }catch (InterruptedException e ){
                    e.printStackTrace();
                }
            }
            System.out.println("M1-END");
        }
        public synchronized void method2(){
            System.out.println("M2-Start");
            for (int i=0; i<10; i++){
                try {
                    Thread.sleep(100);
                }catch (InterruptedException e ){
                    e.printStackTrace();
                }
            }
            System.out.println("M2-END");
        }
    }
// КАК только часть метода сделать синхронизированной? см ниже с помощью Obj  монитора например
    public static class SyncMonitorApp{
        private Object monitor = new Object();

        public static void main(String[] args){
            SyncMonitorApp e2 = new SyncMonitorApp();
            new Thread(() -> e2.method()).start();
            new Thread(() -> e2.method()).start();
            new Thread(() -> e2.method()).start();
        }
        public void method(){
            try {
                System.out.println("NonSyncPart-Begin " + Thread.currentThread().getName());
                for (int i = 0; i < 3; i++){
                    System.out.print('.');
                    Thread.sleep(400);
                }
                System.out.println("NonSyncPart-End " + Thread.currentThread().getName());
                synchronized (monitor){
                    System.out.println("Sync-Begin" + Thread.currentThread().getName());
                    for(int i = 0; i <5; i++){
                        System.out.print(".");
                        Thread.sleep(300);
                    }
                    System.out.println("Sync-End" + Thread.currentThread().getName());
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
/*
// А что если public synchronized STATIC void()  метод?
        тогда метод будет последовательно вызваться но монитором будет выступать сам класс а не объект класса
// Типичная ошибк для счетчика
*/
    public class NotCorrectSyncDoubleCounter{
        private long c1 = 0;
        private long c2 = 0;
        public long value1(){return c1;}
        public long value2(){return c2;}
        public synchronized void inc1(){c1++;}
        public synchronized void inc2(){c2++;}
        public synchronized void dec1(){c1--;}
        public synchronized void dec2(){c2--;}
    }// не корректно тк прарлельно не получ работать с 2мя переменными
    public class CorrectSyncDoubleCounter{
        private long c1 =0;
        private long c2 =0;
        private Object lock1 = new Object(); // !!!
        private Object lock2 = new Object();
        public long value1(){return c1;}
        public long value2(){return c2;}
        // !!!
        public void inc1(){synchronized (lock1){c1++;}}
        public void inc2(){synchronized (lock2){c2++;}}
        public void dec1(){synchronized (lock1){c1--;}}
        public void dec2(){synchronized (lock2){c2--;}}
    }
//  DeadLockApp - захват монитора1 и ожидание монитора2 когда паралельно второй поток сделал захват монитора2 и ожидает монитор1
/*  это бесконечное кольцо и зависание


//  WaitNotify - "немного противная тема тк в джве есть более удобные механизмы"
        Идея: есть потоки каждый печатает свою букву.
        Задача: напечатать буква по порядку алфавита.

 */
    public static class WaitNotifyApp{
        private final Object monitor = new Object();
        // volatile  - изменчивый, не постоянный
        // запрещает кешировать данные в ядрк прцессора ( - = падает прозводительность)
        private volatile char currentLetter = 'A';

        public static void main(String[] args){
            WaitNotifyApp waitNotifyApp = new WaitNotifyApp();
            new Thread(waitNotifyApp::printA).start(); // because static class
            new Thread(() -> {
                waitNotifyApp.printB();
            }).start();
        }
        public void printA(){
            synchronized (monitor){
                try {
                    for(int i =0; i<5; i++) {
                        while (currentLetter != 'A') { // warning exacting not if-else
                            monitor.wait();
                        }
                        System.out.print("A");
                        currentLetter = 'B';
                        monitor.notifyAll();
                    }
                    }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    public void printB(){
        synchronized (monitor){
            try {
                for(int i =0; i<5; i++) {
                    while (currentLetter != 'B') {
                        monitor.wait();
                    }
                    System.out.print("B");
                    currentLetter = 'A';
                    monitor.notifyAll();
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}
}
