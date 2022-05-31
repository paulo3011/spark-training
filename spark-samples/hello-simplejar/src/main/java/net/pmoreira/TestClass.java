package net.pmoreira;

/**
 * This sample class was take from:
 * https://opensource.com/article/22/4/jvm-parameters-java-developers
 * Thanks to: Jayashree Huttanagoudar
 *
 * To emulate OutOfMemoryError:
 * 1) run shadowjar
 * 2) log into spark master node
 * 3) execute: java -XX:+HeapDumpOnOutOfMemoryError -Xms10m -Xmx1g -jar /opt/spark-apps/hello-simplejar-all.jar
 * 4) try: java -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/opt/spark/logs/heap-dumps -Xlog:gc=debug:file=/opt/spark/logs/gctrace-testclass.txt -Xms10m -Xmx1g -jar /opt/spark-apps/hello-simplejar-all.jar
 */

import java.util.ArrayList;
import java.util.List;

public class TestClass {
    public static void main(String[] args) {
        System.out.print("Starting TestClass..");
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < 1000; i++) {
            list.add(new char[1000000]);
        }
        System.out.print("Ending TestClass..");
    }
}