package com.zhangqianli.tbminer.dynamic.chart;

import javafx.collections.FXCollections;

import java.util.HashMap;

public class DynamicBirchTest {
    /**
     * 这Application只能从它的子类的main方法中运行，另外起一个线程运行它不行，垃圾！！！
     * @param args
     */
    public static void main(String[] args){
        Thread t = new Thread(() -> {
            DynamicBirch1 db1 = new DynamicBirch1();
            db1.launch(args);
        });
        t.start();


    }
}
