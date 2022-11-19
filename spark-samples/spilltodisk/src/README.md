# Spark concepts - Spill

## What is a spill to disk in Spark?

"Spark's operators spill data to disk if it does not fit in memory, allowing it to run well on any sized data. 
Likewise, cached datasets that do not fit in memory are either spilled to disk or recomputed on the fly when needed, as determined by the RDD's storage level."
(https://spark.apache.org/faq.html)

Seealso: https://spark.apache.org/docs/latest/rdd-programming-guide.html#rdd-persistence

## Where does spilled data go?

Where spark will save spilled data (from shuffle, cache or partition that don't fit in memory) to disk will depend on:

1) Cluster manager (Standalone, Mesos, YARN, Kubernetes)
   
1.1) YARN

*In cluster mode*: 

The local directories used by the Spark executors and the Spark driver will be the local directories configured for YARN (Hadoop YARN config yarn.nodemanager.local-dirs). 
If the user specifies spark.local.dir, it will be ignored. 

- hadoop.tmp.dir = /tmp/hadoop-${user.name} (https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/core-default.xml)
- yarn.nodemanager.local-dirs = ${hadoop.tmp.dir}/nm-local-dir (seealso: https://hadoop.apache.org/docs/stable/hadoop-yarn/hadoop-yarn-common/yarn-default.xml)

*In client mode*: 

The Spark executors will use the local directories configured for YARN while the Spark driver will use those defined in spark.local.dir. 
This is because the Spark driver does not run on the YARN cluster in client mode, only the Spark executors do.

1.2) Standalone

SPARK_WORKER_DIR = SPARK_HOME/work (Directory to run applications in, which will include both logs and scratch space)

2) Spark configuration (spark.local.dir)

spark.local.dir = /tmp

Directory to use for "scratch" space in Spark, including map output files and RDDs that get stored on disk. This should be on a fast, local disk in your system. It can also be a comma-separated list of multiple
This will be overridden by SPARK_LOCAL_DIRS (Standalone), MESOS_SANDBOX (Mesos) or LOCAL_DIRS (YARN) environment variables set by the cluster manager..

seealso: https://spark.apache.org/docs/3.2.1/configuration.html
