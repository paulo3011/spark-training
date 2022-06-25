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

# Case study 01 - reading one 5.4 GB csv file and checking input partitions

Emulate one file with about 5.4 GB (csv)

Run the man function of java/net/pmoreira/samples/spark/partition/whatis/FakeCsvWriter.java to create the file.

1. Configure the cluster to:
   1. 1 GB for each executors
   2. 4 cores for all executors, and 2 cores per executor => 4/2 = 2 executors
   3. In spark 3.2.1
      1. 60% of (JVM HEAP - 300 MB) is the spark memory fraction
         1. (1000MB-300MB) x 0.60 = 420 MB, this 50% (210MB) is for execution and 50% (210MB) is for storage (cache)
      2. 40% of (JVM HEAP - 300 MB) is for spark and user data
         1. (1000MB-300MB) x 0.40 = 280 MB
   4. 1GB/2 cores per executor = 0.5 GB per core => in spark 3.2.1, by default 60% execution and 40% storage => 250 MB for execution and 250 MB for storage

2. Emulate read this file and see how many partitions will be created (input partitions). 
   1. default input partitions is 128 MB (reading files), then
   2. 5400/128 = 42,18 partitions

Run this run a job to count the number of partitions will be created on read the file.

Job parameters:

if args has --getNumInputPartitions will run df.rdd().getNumPartitions() action to count input partitions
if args has --triggerCount will execute count action on the dataset to count number of records
if args has --checkpointDataset will persist the dataset to disk to be able to see on disk the total of input partitions
if args has --eagerCheckpoint and --checkpointDataset will persist the dataset to disk immediately before any action
if args has --sleepSeconds [number], i.e --sleepSeconds 90 at the end will sleep to keep the spark ui a live for 90 seconds

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
/opt/spark-apps/partition-whatis-all.jar --getNumInputPartitions --sleepSeconds 300
```

Go to http://localhost:4747/ to see that one job was triggered and look at the logs to see something like:

```shell
# 22/06/25 15:23:07 INFO FileSourceScanExec: Planning scan with bin packing, max size: 134217728 bytes, open cost is considered as scanning 4194304 bytes.
# Estimated total partitions: 41
```

Note that in the output log it was shown "...Planning scan with bin packing, max size: 134217728 bytes" = 128 MB

3. Try the count action to check how many jobs will be created and how many tasks will be performed

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
/opt/spark-apps/partition-whatis-all.jar --triggerCount --sleepSeconds 300
```


# Case study 02 - Emulate spill do disk, shuffle and how to solve calculating the spark.sql.shuffle.partitions size

2. Configure the cluster to:
   1. 20 GB for executors (10 GB each)
   2. 7 cores for executors, and 1 cores per executor => 7/1 = 7 executors
   3. 20/7 = 2.85 GB per core => 1.428 GB for execution and 1.428 GB for storage
3. Emulate read this file and see how many partitions will be created (input partitions). Discovery how to see this.
   1. default input partitions is 128 MB (reading files), then
   2. 5400/128 = 42,18 partitions
   3. try count action to check this
4. Emulate one shuffle (sort) with shuffle partitions size set with custom value. If we want shuffle partitions around 300 MB, then:
   1. 5000/300 = 16.6 (partitions)
   2. 16.6 partitions / 7 cores = 2.37 => more than two partitions per core. Then 7x2 = 14 (desirable partitions)
   3. 14 partitions with 7 cores means we will have 2 cycles of executions because 16.6 - 14 = 2.6 (more than 2 partition will run in the second cycle)
   4. set spark.sql.shuffle.partitions to 14
   5. Check for spillage to disk
5. Then, emulate another configuration to eliminate the spill
   1. 20 GB for executors
   2. 7 cores for executors, and 1 cores per executor => 7/1 = 7 executors
   3. 20/7 = 2.85 GB per core => 1.428 GB for execution and 1.428 GB for storage
   4. Partitions shuffle
      1. 5000/100 = 50 (partitions)
      2. 50/7 = 7.14 => 7x7 = 49 (desirable partitions)
      3. 50-49 = 1 (one partition left for the second cycle)
      4. set spark.sql.shuffle.partitions to 49
      5. Check for spillage to disk
      6. Check if the size that matters is the full size of dataset, in that case 5GB and even though the input partition is 128 MB our shuffle partition calculation was important




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