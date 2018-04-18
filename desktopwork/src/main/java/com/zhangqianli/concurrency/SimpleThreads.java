package com.zhangqianli.concurrency;

public class SimpleThreads {
    static void threadMessage(String message) {
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }

    private static class MessageLoop implements Runnable {

        @Override
        public void run() {
            String importantInfo[] = {"Mares eat oats",
                    "Does eat oats",
                    "Little lamps eat ivy",
                    "A kid will eat ivy too"
            };
            for (int i=0;i<importantInfo.length;i++){
                try {
                    Thread.sleep(4000);
                    threadMessage(importantInfo[i]);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                    e.printStackTrace();
                    threadMessage("I wasn't done!");
                }
            }
        }
        public static void main(String[] args) throws InterruptedException{
            long patience = 1000*60;
            if (args.length>0){
                patience = Long.parseLong(args[0])*1000;
            }
            threadMessage("Starting MessageLoop thread");
            long startTime = System.currentTimeMillis();
            Thread t = new Thread(new MessageLoop());
            t.start();
            threadMessage("Waiting for MessageLoop to finish");
            while (t.isAlive()){
                threadMessage("Still waiting...");
                t.join(1000);
                t.interrupt();
                if((System.currentTimeMillis()-startTime) > patience && t.isAlive()){
                    threadMessage("Tired of waiting!");
                    t.interrupt();
                    t.join();
                }
            }
            threadMessage("Finally");
        }
    }
}
