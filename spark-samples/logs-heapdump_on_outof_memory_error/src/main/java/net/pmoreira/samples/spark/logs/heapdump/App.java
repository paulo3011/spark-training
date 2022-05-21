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

import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        System.out.println("starting spark app");

        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("Hello-World-Spark-Logs-DumpOnError");

        //use this master if you haven't install spark on you local ubuntu
        sparkConf.set("spark.master","local[*]");
        //if you have install spark on your local ubuntu
        //sparkConf.set("spark.master","spark://localhost:7077");
        //A string of extra JVM options to pass to executors. This is intended to be set by users. For instance, GC settings or other logging.
        sparkConf.set("spark.executor.extraJavaOptions","-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/spark/heapDumps");
        //Amount of memory to use per executor process, in the same format as JVM memory strings with a size unit suffix ("k", "m", "g" or "t") (e.g. 512m, 2g). Default 1g
        sparkConf.set("spark.executor.memory","1g");
        /*
            The number of cores to use on each executor. In standalone and Mesos modes:
            - default: all available; only on worker
         */
        sparkConf.set("spark.executor.cores", "1");
        //Whether to log Spark events, useful for reconstructing the Web UI after the application has finished.
        sparkConf.set("spark.eventLog.enabled", "true");
        //Base directory in which Spark events are logged, if spark.eventLog.enabled is true. Within this base directory, Spark creates a sub-directory for each application, and logs the events specific to the application in this directory. Users may want to set this to a unified location like an HDFS directory so history files can be read by the history server.
        //befor you need to create the dir, e.g: mkdir /tmp/spark/spark-events
        sparkConf.set("spark.eventLog.dir", "/tmp/spark/spark-events");

        //If true, spark application running in client mode will write driver logs to a persistent storage, configured in spark.driver.log.dfsDir.
        sparkConf.set("spark.driver.log.persistToDfs.enabled","true");
        //Base directory in which Spark driver logs are synced, if spark.driver.log.persistToDfs.enabled is true.
        //remember: to avoid /tmp/spark/driver-logs/ does not exist. Please create this dir in order to persist driver logs
        sparkConf.set("spark.driver.log.dfsDir","/tmp/spark/driver-logs/");

        try (SparkSession sparkSession = SparkSession
                .builder()
                .config(sparkConf)
                .getOrCreate()) {

            Thread.sleep(1000 * 30);

            //create a spark java context to be able to work with spark java class
            JavaSparkContext javaSparkContext = JavaSparkContext.fromSparkContext(sparkSession.sparkContext());

            //create a java rdd from an in memory csv dataset
            JavaRDD<String> javaRDD = createJavaRdd(javaSparkContext);

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

            //cache the dataframe to see his size in memory on spark-ui/storage
            dataframe = dataframe.persist(StorageLevel.MEMORY_ONLY());

            long total = dataframe.count();

            System.out.printf("\ntotal of records: %s \n\n", total);

            //print on output screen 3 rows of the dataframe
            dataframe.show(3);

            System.out.println("Go to the Web browser and see the spark ui running. http://localhost:4040");
            /*
            Try to look at: localhost:4040 or see on the run output log something like:
            SparkUI: Bound SparkUI to 0.0.0.0, and started at http://172.17.95.117:4040.
            Go to Environment/Spark Properties and search for spark.executor.extraJavaOptions
            to see if your settings was applied.
             */
            int sleepSeconds = 120;
            Thread.sleep(1000 * sleepSeconds);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("=>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>> >>>>>>>>>");
        System.out.println("Ended SparkSession..");
        System.out.println("=>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>> >>>>>>>>>");
    }

    private static JavaRDD<String> createJavaRdd(JavaSparkContext javaSparkContext) {
        //will create a dataset of approximately 512 MB
        return javaSparkContext.parallelize(createCsvDataset(512000));
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