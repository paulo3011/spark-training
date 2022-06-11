# Road map

## Pocs and samples about

### Spark concepts

1. What is a task in Spark? How to visualize how many transformations are executed in one task?
2. What is partitions? How to set up? Based on the number of cores in the cluster? (see Chapter 19 in Spark The Definitive Guide)

### Spark logs

1. Setup spark history server to be able to view the spark driver ui for each application executed.

### Tuning

1. How to measure the amount of memory needed for a task?

- https://spark.apache.org/docs/0.6.0/tuning.html (kryo, memory size, parallelism, gc tuning)
- https://spark.apache.org/docs/latest/tuning.html (new method size estimate for memory size)

2. How to view the real parallelism using coalesce with many partitions and executors? Will each executor run in parallel the coalesce command?