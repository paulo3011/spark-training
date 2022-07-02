# Spark concepts - Partition - Splliting partitions into groups

# Case study 01 - checking how many partitions will be created and partitions size for group by operations

Emulate one file with about +- 800 MB (csv) with:
- data for two groups of key (diviceID 1 with 600 GB and diviceID 2 with 200 MB)

Run the man function of java/net/pmoreira/samples/spark/partition/groupbykey/CreateFiles.java to create the files.

1. Configure the cluster to:
   1. 3 GB for each executor
   2. 4 cores for all executors, and 2 cores per executor => 4/2 = 2 executors
   3. This will give us 6 GB of memory to process 2.2 GB

2. Run this run a job to count the number of partitions will be created on read the file.

Job parameters:

* if args has --getNumInputPartitions will run df.rdd().getNumPartitions() action to count input partitions
* if args has --triggerGroupByKey will execute groupByKey action on the dataset 
* if args has --triggerCount will execute count action on the dataset to count number of records
* if args has --sleepSeconds [number], i.e --sleepSeconds 90 at the end will sleep to keep the spark ui a live for 90 seconds

```shell
docker exec -it spark-master bash
spark-submit --class net.pmoreira.samples.spark.partition.groupbykey.App \
--deploy-mode client \
--master spark://spark-master:7077 \
--verbose \
--driver-memory 3g \
--driver-cores 1 \
--driver-java-options "-XX:OnOutOfMemoryError='kill -9 %p'" \
--conf spark.driver.log.persistToDfs.enabled=true \
--conf spark.driver.log.dfsDir=/opt/spark/logs/ \
--conf "spark.executor.extraJavaOptions=-verbose:gc -Xlog:gc=debug:file=/opt/spark/logs/-executorgclog.txt -XX:OnOutOfMemoryError='kill -9 %p'" \
--executor-memory 3g \
--total-executor-cores 4 \
--executor-cores 2 \
/opt/spark-apps/partition-groupbykey-all.jar --groupByKey --triggerCount --sleepSeconds 600
```

Go to http://localhost:4747/ to see that one job was triggered
