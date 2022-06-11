package net.pmoreira.samples.spark.tasks.whatis;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkEnv;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import scala.Function1;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        System.out.println("starting spark app");

        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("What is task");

        try (SparkSession sparkSession = SparkSession
                .builder()
                .config(sparkConf)
                .getOrCreate()) {

            System.out.println("Started sparksession");

            //create a spark java context to be able to work with spark java class
            JavaSparkContext javaSparkContext = JavaSparkContext.fromSparkContext(sparkSession.sparkContext());

            System.out.println("Started JavaSparkContext");

            //create a java rdd from an in memory csv dataset
            JavaRDD<String> javaRDD = createJavaRdd(javaSparkContext);

            System.out.printf("Created JavaRDD with %s partitions \n", javaRDD.rdd().getNumPartitions());

            JavaRDD<Row> rowsRdd = javaRDD.map(row -> {
                String[] csvColumns = row.split(",");
                return RowFactory.create(Integer.parseInt(csvColumns[0]), csvColumns[1]);
            }).setName("Tranformation 2 - Convert raw csv line to Row object");

            //create a schema for the dataset to be able to create dataframe
            StructType schema = new StructType()
                    .add("number", DataTypes.IntegerType)
                    .add("text", DataTypes.StringType);

            //create a dataframe
            Dataset<Row> dataframe = sparkSession.createDataFrame(rowsRdd, schema);

            //transform the dataframe to have word count column and make the task duration increase with sleep inside
            StructType schema2 = new StructType()
                    .add("number", DataTypes.IntegerType)
                    .add("text", DataTypes.StringType)
                    .add("total_words", DataTypes.IntegerType)
                    .add("task_attempt_Id", DataTypes.LongType)
                    .add("partition_Id", DataTypes.IntegerType)
                    .add("transformation_date", DataTypes.StringType)
                    .add("executor_id", DataTypes.StringType)
                    ;

            ExpressionEncoder<Row> encoder = RowEncoder.apply(schema2);

            dataframe = dataframe.map(new MapFunction<Row, Row>() {
                @Override
                public Row call(Row row) throws Exception {
                    TaskContext tc = TaskContext.get();
                    SparkEnv sparkEnv = SparkEnv.get();
                    String executorId = sparkEnv.executorId();
                    LocalDateTime currentDate = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String line = row.get(1).toString();
                    String[] words = line.split(" ");
                    int numberOfWords = words.length;
                    return RowFactory.create(row.get(0), row.get(1), numberOfWords, tc.taskAttemptId(), tc.partitionId(), currentDate.format(formatter), executorId);
                }
            }, encoder);


            //print in the output the execution plan
            dataframe.explain();

            //start the job (call an action)
            dataframe.write()
                    .mode(SaveMode.Overwrite)
                    .format("csv")
                    .option("header","true")
                    .save("/opt/spark-data/out/whatis-task")

            ;

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

    private static JavaRDD<String> createJavaRdd(JavaSparkContext javaSparkContext) {
        /**
         * 100000 records outputs 10 MB
         * INFO MemoryStore: Block broadcast_0 stored as values in memory (estimated size 216.2 KiB)
         * INFO BlockManagerInfo: Added broadcast_0_piece0 in memory on ea4e57caede6:36075 (size: 78.6 KiB)
         *
         * 30*100000 :
         * 22/06/11 17:00:22 INFO MemoryStore: Block broadcast_0 stored as values in memory (estimated size 216.2 KiB, free 434.2 MiB)
         * 22/06/11 17:00:22 INFO MemoryStore: Block broadcast_0_piece0 stored as bytes in memory (estimated size 78.6 KiB, free 434.1 MiB)
         * 22/06/11 17:00:22 INFO BlockManagerInfo: Added broadcast_0_piece0 in memory on ea4e57caede6:40119 (size: 78.6 KiB, free: 434.3 MiB)
         *
         * 22/06/11 17:00:22 WARN TaskSetManager: Stage 0 contains a task of very large size (105364 KiB). The maximum recommended task size is 1000 KiB.
         * 22/06/11 17:00:22 INFO TaskSetManager: Starting task 0.0 in stage 0.0 (TID 0) (172.19.0.4, executor 1, partition 0, PROCESS_LOCAL, 107893343 bytes) taskResourceAssignments Map()
         * 22/06/11 17:00:23 INFO TaskSetManager: Starting task 1.0 in stage 0.0 (TID 1) (172.19.0.3, executor 0, partition 1, PROCESS_LOCAL, 109004453 bytes) taskResourceAssignments Map()
         */
        ArrayList<String> list = createCsvDataset(30*100000);
        System.out.println("Created arraylist, will parallelize to create RDD with 3 partitions");
        return javaSparkContext.parallelize(list, 3).setName("Tranformation 1 - Parallelize ArrayList<String> to JavaRDD<String>");
    }

    private static ArrayList<String> createCsvDataset(int numberLines){
        ArrayList<String> dataset = new ArrayList<>();
        String myString = "Spark performs as many steps as it can at one point in time before writing data to memory or disk.";
        for (int i = 0; i < numberLines; i++) {
            dataset.add(String.format("%s,%s",i, myString));
        }
        return dataset;
    }
}


