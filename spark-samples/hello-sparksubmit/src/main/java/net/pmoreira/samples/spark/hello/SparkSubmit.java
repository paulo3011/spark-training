package net.pmoreira.samples.spark.hello;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

public class SparkSubmit {
    /**
     * 1) copia o jar para o master e worker
     * docker cp hello-sparksubmit-all.jar spark-test-master:/tmp/hello-sparksubmit-all.jar
     * docker cp hello-sparksubmit-all.jar spark-test-worker:/tmp/hello-sparksubmit-all.jar
     * or mount volume
     *
     * To submit a jar spark application to standalone cluster through spark-submit:
     * Note: first need to ssh into master node.
     *
     * spark-submit --class net.pmoreira.samples.spark.hello.SparkSubmit --deploy-mode cluster --master spark://172.17.0.3:7077 /tmp/hello-sparksubmit-all.jar
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("starting spark app");

        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("Hello world - Spark-Submit");

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
