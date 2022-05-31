package net.pmoreira;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

public class App {
    public static void main(String[] args) {
        System.out.println("starting spark app");

        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("Hello-World--Start-Spark");
        //https://spark.apache.org/docs/latest/submitting-applications.html#master-urls
        sparkConf.set("spark.master","local[*]");

        try (SparkSession sparkSession = SparkSession
                .builder()
                .config(sparkConf)
                .getOrCreate()) {

            System.out.println("Go to the Web browser and see the spark ui running. http://localhost:4040");
            /*
            Try to look at: localhost:4040 or see on the run output log something like:
            SparkUI: Bound SparkUI to 0.0.0.0, and started at http://172.17.95.117:4040.
            Look at different tabs to get know what in there. E.g:
            - Executors: (How many executors are running)
            - Environment/Resource Profiles (How many memory each executor request?)
             */
            int sleepSeconds = 180;
            Thread.sleep(1000 * sleepSeconds);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("=>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>> >>>>>>>>>");
        System.out.println("Ended SparkSession..");
        System.out.println("=>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>> >>>>>>>>>");
    }
}