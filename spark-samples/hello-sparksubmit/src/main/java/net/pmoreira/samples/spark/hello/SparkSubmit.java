package net.pmoreira.samples.spark.hello;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

public class SparkSubmit {
    /**
     * 1) Copy the jar to master and worker nodes or mount volume:
     *
     *  docker cp hello-sparksubmit-all.jar spark-test-master:/tmp/hello-sparksubmit-all.jar
     *  docker cp hello-sparksubmit-all.jar spark-test-worker:/tmp/hello-sparksubmit-all.jar
     *
     *
     * 2) To submit a jar spark application to standalone cluster through spark-submit:
     * Note: first need to ssh into master node or: docker exect -it spark-master bash.
     *
     * spark-submit --class net.pmoreira.samples.spark.hello.SparkSubmit --deploy-mode cluster --master spark://spark-master:7077 /tmp/hello-sparksubmit-all.jar
     *
     * Or seealso: spark-samples/docker/build/README.md | spark-samples/docker/build/docker-compose.yml
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

            System.out.println("Making spark job sleep for 3 minutes to be able to see the spark ui running at http://localhost:4747 or http://localhost:4848 or http://localhost:4949");

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
