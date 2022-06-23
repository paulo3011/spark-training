# Spark concepts - Partition

## What is a partition in Spark?

## What types of partitions exists?

## How we can process a partition?

1. Spark always operate on the entire partition individually
2. We can use functions from RDD and Dataframe API like map, mapPartitions and flatMap
   1. map
      - Allows to operate on each row of the partition
   2. mapPartitions
      - Allows us to perform an operation on that entire partition. 
      - This is valuable for performing something on an entire subdataset of your RDD (each partition represented as an iterator).
   3. flatMap 
      - Similar to map, but each input item can be mapped to 0 or more output items (so func should return a Seq rather than a single item)
3. Map is an alias to mapPartitions
4. You can gather all values of a partition class or group into one partition and then operate on that entire group using arbitrary functions and controls.

Chambers, Bill; Zaharia, Matei. Spark: The Definitive Guide: Big Data Processing Made Simple (p. 282). O'Reilly Media. Edição do Kindle.



Submit the sample spark jar application (partition-whatis-all.jar) in client mode from master node

```shell
docker exec -it spark-master bash
spark-submit --class net.pmoreira.samples.spark.partition.whatis.App \
--deploy-mode client \
--master spark://spark-master:7077 \
--verbose \
--driver-memory 3g \
--driver-cores 1 \
--driver-java-options "-XX:OnOutOfMemoryError='kill -9 %p'" \
--conf spark.driver.log.persistToDfs.enabled=true \
--conf spark.driver.log.dfsDir=/opt/spark/logs/ \
--conf "spark.executor.extraJavaOptions=-verbose:gc -Xlog:gc=debug:file=/opt/spark/logs/-executorgclog.txt -XX:OnOutOfMemoryError='kill -9 %p'" \
--executor-memory 1g \
--total-executor-cores 4 \
--executor-cores 2 \
/opt/spark-apps/partition-whatis-all.jar
```

```shell
docker exec -it spark-master bash
spark-submit --class net.pmoreira.samples.spark.partition.whatis.App \
--deploy-mode client \
--master spark://spark-master:7077 \
--verbose \
--driver-memory 3g \
--driver-cores 1 \
--driver-java-options "-XX:OnOutOfMemoryError='kill -9 %p'" \
--conf spark.driver.log.persistToDfs.enabled=true \
--conf spark.driver.log.dfsDir=/opt/spark/logs/ \
--conf "spark.executor.extraJavaOptions=-verbose:gc -Xlog:gc=debug:file=/opt/spark/logs/-executorgclog.txt -XX:OnOutOfMemoryError='kill -9 %p'" \
--executor-memory 1g \
--total-executor-cores 1 \
--executor-cores 1 \
/opt/spark-apps/partition-whatis-all.jar
```