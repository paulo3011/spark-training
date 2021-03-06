package net.pmoreira.samples.spark.partition.whatis;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

import java.util.Arrays;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        System.out.printf("Starting spark app with args: %s", Arrays.toString(args));

        //this way of get configuration is just for simplicity. We can use libraries like com.beust.jcommander.Parameter
        /*
            if args has --getNumInputPartitions will run df.rdd().getNumPartitions() action to count input partitions
            if args has --triggerCount will execute count action on the dataset to count number of records
            if args has --checkpointDataset will persist the dataset to disk (/opt/spark-data/checkpoint/) in binary format be able to see on disk the total of input partitions
            if args has --eagerCheckpoint and --checkpointDataset will persist the dataset to disk immediately before any action
            if args has --sleepSeconds [number], i.e --sleepSeconds 90 at the end will sleep to keep the spark ui a live for 90 seconds
            if args has --writeToDisk [outputpath] will save on disk the dataset in csv format
         */
        boolean getNumInputPartitions = Arrays.stream(args).anyMatch("--getNumInputPartitions"::equals);
        boolean triggerCount = Arrays.stream(args).anyMatch("--triggerCount"::equals);
        boolean checkpointDataset = Arrays.stream(args).anyMatch("--checkpointDataset"::equals);
        boolean eagerCheckpoint = Arrays.stream(args).anyMatch("--eagerCheckpoint"::equals);
        boolean hasSleepSeconds = Arrays.stream(args).anyMatch("--sleepSeconds"::equals);
        int sleepSeconds = 60*6;
        boolean writeToDisk = Arrays.stream(args).anyMatch("--writeToDisk"::equals);
        String outputPath = "/opt/spark-data/outputdir/hugecsvfile";

        if(hasSleepSeconds){
            try {
                int sleepSecondsIndex = Arrays.stream(args).collect(Collectors.toList()).indexOf("--sleepSeconds");
                int sleepSecondsConfigValueIndex = sleepSecondsIndex + 1;
                if (args.length > sleepSecondsConfigValueIndex)
                    sleepSeconds = Integer.parseInt(args[sleepSecondsConfigValueIndex]);
            }catch (Exception ex){
                //ignore sleep configuration errors
            }
        }

        if(writeToDisk){
            try {
                int writeToDiskIndex = Arrays.stream(args).collect(Collectors.toList()).indexOf("--writeToDisk");
                int writeToDiskConfigValueIndex = writeToDiskIndex + 1;
                if (args.length > writeToDiskConfigValueIndex)
                    outputPath = args[writeToDiskConfigValueIndex];
            }catch (Exception ex){
                //ignore writeToDisk configuration errors
            }
        }

        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("What is partition");

        try (SparkSession sparkSession = SparkSession
                .builder()
                .config(sparkConf)
                .getOrCreate()) {

            System.out.println("Started sparksession");

            String filePath = "/opt/spark-data/hugefile.csv";
            Dataset<Row> df = sparkSession.read().csv(filePath);

            System.out.printf("Configured the dataframe to read: %s \n", filePath);

            if(getNumInputPartitions) {
                int totalPartitions = df.rdd().setName("rdd_hugefile.csv").getNumPartitions();
                System.out.printf("Estimated total partitions: %s \n", totalPartitions);
            }

            if(checkpointDataset) {
                System.out.printf("Will checkpoint the dataset with eager=%s", eagerCheckpoint);
                //to see the input partitions files in disk, persist the RDD partitions files in disk
                sparkSession.sparkContext().setCheckpointDir("/opt/spark-data/checkpoint/");
                df.checkpoint(eagerCheckpoint); //eager=true (checkpoint this DataFrame immediately)
            }

            if(writeToDisk) {
                System.out.printf("Will write to disk in: %s \n", outputPath);
                df.write().format("csv").mode(SaveMode.Overwrite).save(outputPath);
            }

            if(triggerCount) {
                //trigger an action to perform a job and make things happen
                long totalRecords = df.count();
                System.out.printf("Total records in the dataframe: %s \n", totalRecords);
            }

            //force the spark driver ui wait to be able to visualize after the job completes
            Thread.sleep(1000 * sleepSeconds);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("=>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>> >>>>>>>>>");
        System.out.println("Ended SparkSession..");
        System.out.println("=>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>> >>>>>>>>>");
    }


}
