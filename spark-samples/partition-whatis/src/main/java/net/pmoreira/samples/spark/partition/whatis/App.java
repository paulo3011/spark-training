package net.pmoreira.samples.spark.partition.whatis;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class App {
    public static void main(String[] args) {
        System.out.println("starting spark app");

        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("What is partition");

        try (SparkSession sparkSession = SparkSession
                .builder()
                .config(sparkConf)
                .getOrCreate()) {

            System.out.println("Started sparksession");

            //TODO ENTENDER PQ COM 1 EXECUTOR EXECUTOU 2 JOBS PARA CADA ETAPA VER SE COM MAIS EXECUTOR FAZ ISSO

            Dataset<Row> df = sparkSession.read().csv("/opt/spark-data/hugefile.csv");

            int totalPartitions = df.rdd().getNumPartitions();

            System.out.printf("Estimated total partitions: %s \n", totalPartitions);

            //to see the input partitions files in disk
            sparkSession.sparkContext().setCheckpointDir("/opt/spark-data/checkpoint/");
            df.checkpoint();
            long totalRecords = df.count();

            System.out.printf("Total records in the dataframe: %s \n", totalRecords);

            //force the spark driver ui wait to be able to visualize after the job completes
            int sleepSeconds = 60*6;
            Thread.sleep(1000 * sleepSeconds);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("=>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>> >>>>>>>>>");
        System.out.println("Ended SparkSession..");
        System.out.println("=>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>> >>>>>>>>>");
    }


}
