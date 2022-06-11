package net.pmoreira.samples.spark.logs.heapdump;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;

import java.io.Serializable;
import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        System.out.println("starting spark app");

        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("Hello-World-Spark-Logs-DumpOnError");
        //set spark.rpc.message.maxSize to avoid rpc error
        sparkConf.set("spark.rpc.message.maxSize", "2000");//2047MB

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

            System.out.println("Created JavaRDD");

            JavaRDD<Row> rowsRdd = javaRDD.map(row -> {
               String[] csvColumns = row.split(",");
                return RowFactory.create(Integer.parseInt(csvColumns[0]), csvColumns[1]);
            });

            //create a schema for the dataset to be able to create dataframe
            StructType schema = new StructType()
                    .add("number", DataTypes.IntegerType)
                    .add("text", DataTypes.StringType);

            //create a dataframe
            Dataset<Row> dataframe = sparkSession.createDataFrame(rowsRdd, schema);

            //start the job (call an action)
            long total = dataframe.count();

            System.out.printf("\ntotal of records: %s \n\n", total);
        }

        System.out.println("=>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>> >>>>>>>>>");
        System.out.println("Ended SparkSession..");
        System.out.println("=>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>> >>>>>>>>>");
    }

    private static JavaRDD<String> createJavaRdd(JavaSparkContext javaSparkContext) {
        //will create a cvs with 512000 lines and the jvm heap set to 1G will have a peak of 0.96GB and will throw OnOutOfMemoryError
        ArrayList<String> list = createCsvDataset(512000);
        System.out.println("Created arraylist, will parallelize to create RDD");
        return javaSparkContext.parallelize(list);
    }

    private static ArrayList<String> createCsvDataset(int numberLines){
        ArrayList<String> dataset = new ArrayList<>();
        String oneKbString = "Spark performs as many steps as it can at one point in time before writing data to memory or disk. With pipelining, any sequence of operations that feed data directly into each other, without needing to move it across nodes, is collapsed into a single stage of tasks that do all the operations together. For example, if you write an RDD-based program that does a map, then a filter, then another map, these will result in a single stage of tasks that immediately read each input record, pass it through the first map, pass it through the filter, and pass it through the last map function if needed. Chambers, Bill; Zaharia, Matei. Spark: The Definitive Guide: Big Data Processing Made Simple (p. 329). O'Reilly Media. Edição do Kindle.";
        for (int i = 0; i < numberLines; i++) {
            dataset.add(String.format("%s,%s",i, oneKbString));
        }
        return dataset;
    }
}
