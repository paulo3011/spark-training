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

### Spark logs

1. Setup spark history server to be able to view the spark driver ui for each application executed.

### Tuning

1. How to measure the amount of memory needed for a task?

- https://spark.apache.org/docs/0.6.0/tuning.html (kryo, memory size, parallelism, gc tuning)
- https://spark.apache.org/docs/latest/tuning.html (new method size estimate for memory size)

2. How to view the real parallelism using coalesce with many partitions and executors? Will each executor run in parallel the coalesce command?