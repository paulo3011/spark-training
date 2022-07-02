# Road map

## Pocs and samples about

### Spark concepts

1. What is partitions? How to set up? Based on the number of cores in the cluster? (see Chapter 19 in Spark The Definitive Guide)
   1. Emulate one file with 5 GB (csv)
   2. Configure the cluster to:
      1. 20 GB for executors
      2. 7 cores for executors, and 1 cores per executor => 7/1 = 7 executors
      3. 20/7 = 2.85 GB per core => 1.428 GB for execution and 1.428 GB for storage
   3. Emulate read this file and see how many partitions will be created (input partitions). Discovery how to see this.
      1. default input partitions is 128 MB (reading files), then
      2. 5000/128 = 39,06 partitions
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

### Memory utilization

1. If I have one big file with about 5GB and reading with default input partition size 128 MB (about 41 partitions) and have how spark will process the entire dataset in memory?
   1. will process partition by partition (1 partition (128MB) per 1 task per 1 executor per 1 core (cpu))
   2. So if we have 2 cores and 1 executor => 2x128MB = 256 MB at least will necessary for partition data
2. Spark puts the partition data in what memory region? Execution? Storage? or User data region?
3. R: Execution because storage is meant for cache and when storage is full this memory can't be used.
4. What is the minimal memory each executor needs?
   1. 1.5 * reserved Memory (300MB) = > 450MB of heap
   2. https://www.linkedin.com/pulse/apache-spark-memory-management-deep-dive-deepak-rajak
5. Explain each memory region
   1. Reserved
      1. Definition: Fixed (hardcode) memory used by the system. The value is 300MB.
   2. Unified (Exection and Storage)       
      1. This is the memory managed by Spark. The default is (Java Heap – 300MB) * 0.6.
      2. Applications that do not use caching can use the entire space for execution, __obviating unnecessary disk spills__.
      3. For intance, If we give for executor 1GB => 1000-300 x 06 => 700 x 0.6 => Unified Memory = 420 MB
         1. Execution
            1. Default is 50% of Unified Memory (i.e 420/2 = 210 MB)
            2. Refers to that used for computation in shuffles, joins, sorts and aggregations, used to buffer intermidiate results of your transformations
            3. Execution memory tends to be more short-lived and evicted immediately after each operation
         2. Storage
            1. Default is 50% of Unified Memory (i.e 420/2 = 210 MB)
            2. Refers to that used for caching and propagating internal data across the cluster (i.e. broadcasts variables)
            3. Is the storage space within M (Unified Memory) where cached blocks immune to being evicted by execution.
            4. The configuration spark.memory.storageFraction can change the percentage of memory used for storage and increase or decrease execution memory
            7. Two main functions handle the persistence of data RDD's cache() and persist(). Cache() is an alias for persist(StorageLevel.MEMORY_ONLY)
            8. When execution memory is not used, storage can acquire all the available memory and vice versa
   3. User data 
      1. Where this exists? On each process (driver, executors)?
         1. Driver and executors
      2. How can use this?
      3. How can I distinguish when in my code this space will be used?
         1. All code outside map or transformation? Or any space need by my code without consider partition data and spark data?
      4. Is the space where our code can save data to memory (no internal spark management or control)
      5. What is the size?
         1. (Java Heap – 300MB) * 0.40
         2. Note: Not respecting this boundary in your code might cause OOM error.

References:
- https://spark.apache.org/docs/latest/tuning.html#tuning-data-structures
- https://www.linkedin.com/pulse/apache-spark-memory-management-deep-dive-deepak-rajak
- https://www.youtube.com/watch?v=dPHrykZL8Cg


### Parallelism

- Undertand how to control parallelism, to tests with spark.default.parallelism and cores per executors. 
- It is possible to execute more than 1 task if the executor has just one core? If we limit the number of cores of each executor is this the static assignment and if we leave the core limit undefined is this the dynamic approach?


### Directories

- Build a sample with spark-wharehouse dir: https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/sql/hive/SparkHiveExample.scala

### Spark logs

1. Setup spark history server to be able to view the spark driver ui for each application executed.

### Tuning

1. How to measure the amount of memory needed for a task?

- https://spark.apache.org/docs/0.6.0/tuning.html (kryo, memory size, parallelism, gc tuning)
- https://spark.apache.org/docs/latest/tuning.html (new method size estimate for memory size)

2. How to view the real parallelism using coalesce with many partitions and executors? Will each executor run in parallel the coalesce command?

References:

- https://databricks.com/session_na20/the-apache-spark-file-format-ecosystem