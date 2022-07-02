package net.pmoreira.samples.spark.partition.groupbykey;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        System.out.printf("Starting spark app with args: %s", Arrays.toString(args));

        //this way of get configuration is just for simplicity. We can use libraries like com.beust.jcommander.Parameter
        /*
            if args has --getNumInputPartitions will run df.rdd().getNumPartitions() action to count input partitions
            if args has --groupByKey will execute group by key action on the dataset
            if args has --triggerCount will execute count action on the grouped dataset to count number of records
            if args has --triggerWriteToDisk will execute write action (save to disk) on the grouped dataset
            if args has --sleepSeconds [number], i.e --sleepSeconds 90 at the end will sleep to keep the spark ui a live for 90 seconds
         */
        boolean getNumInputPartitions = Arrays.stream(args).anyMatch("--getNumInputPartitions"::equals);
        boolean groupByKey = Arrays.stream(args).anyMatch("--groupByKey"::equals);
        boolean triggerCount = Arrays.stream(args).anyMatch("--triggerCount"::equals);
        boolean triggerWriteToDisk = Arrays.stream(args).anyMatch("--triggerWriteToDisk"::equals);
        boolean hasSleepSeconds = Arrays.stream(args).anyMatch("--sleepSeconds"::equals);
        int sleepSeconds = 60*6;

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

        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("Group partition by key");

        try (SparkSession sparkSession = SparkSession
                .builder()
                .config(sparkConf)
                .getOrCreate()) {

            System.out.println("Started sparksession");

            String filePath = "/opt/spark-data/all-devices.csv";
            StructType schema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("device_id", DataTypes.StringType, false),
                DataTypes.createStructField("event_description", DataTypes.StringType, false)
            });
            Dataset<Row> df = sparkSession.read().schema(schema).csv(filePath);
            //print 2 lines into output console
            df.show(2);

            System.out.printf("Configured the dataframe to read: %s", filePath);

            if(getNumInputPartitions) {
                int totalPartitions = df.rdd().setName("rdd_hugefile.csv").getNumPartitions();
                System.out.printf("Estimated total input partitions: %s \n", totalPartitions);
            }

            if(groupByKey) {
                System.out.printf("Will execute groupbykey");

                JavaPairRDD<String, Row> keyValueRDD = df.rdd().toJavaRDD().mapPartitionsToPair(new PairFlatMapFunction<Iterator<Row>, String, Row>() {
                    @Override
                    public Iterator<Tuple2<String, Row>> call(Iterator<Row> rowIterator) throws Exception {
                        ArrayList<Tuple2<String, Row>> list = new ArrayList<>();

                        while (rowIterator.hasNext()) {
                            Row row = rowIterator.next();
                            Tuple2<String, Row> tuple = new Tuple2<>(row.getAs("device_id"), row);
                            list.add(tuple);
                        }

                        return list.iterator();
                    }
                });

                //trigger an action to perform a job and make things happen
                JavaPairRDD<String, Iterable<Row>> groupedRdd = keyValueRDD.groupByKey();
                //WARN SparkContext: Spark is not running in local mode, therefore the checkpoint directory must not be on the local filesystem. Directory '/opt/spark-data/checkpoint/groupByKey/' appears to be on the local filesystem.
                sparkSession.sparkContext().setCheckpointDir("hdfs://namenode:8020/user/spark-data/checkpoint/groupByKey/");
                groupedRdd.checkpoint();

                if(triggerCount) {
                    //trigger an action to perform a job and make things happen
                    long totalRecords = groupedRdd.count();
                    System.out.printf("Total records in the groupedRdd RDD: %s \n", totalRecords);
                }

                /*
                if(triggerWriteToDisk) {
                    //start the job (call an action)
                    groupedRdd.write()
                            .mode(SaveMode.Overwrite)
                            .format("csv")
                            .option("header", "true")
                            .save("/opt/spark-data/out/whatis-task");
                }
                */
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

