Spark Docker files usable for testing and development purposes.

1. Build docker images for Spark 3.2.1

```shell
docker build -t spark-test-base base/
docker build -t spark-test-master master/
docker build -t spark-test-worker worker/
```

2. Run docker compose

```shell
docker compose up
```

3. Check if the apps are present in master node

```shell
# log into master shell
docker exec -it spark-master bash
# check if the apps are present
ls /opt/spark-apps
# hello-sparksubmit-all.jar
```
4. Submit a sample spark jar application in cluster mode

```shell
spark-submit --class net.pmoreira.samples.spark.hello.SparkSubmit --deploy-mode cluster --master spark://spark-master:7077 /opt/spark-apps/hello-sparksubmit-all.jar
```

5. Open in browser http://localhost:4747 or http://localhost:4848 or http://localhost:4949 to see the Driver UI
   
Note: spark job will sleep for 3 minutes and then will finish

6. Submit a sample spark jar application in client mode from master node

```shell
spark-submit --class net.pmoreira.samples.spark.hello.SparkSubmit --deploy-mode client --master spark://spark-master:7077 /opt/spark-apps/hello-sparksubmit-all.jar
```

6. Open in browser http://localhost:4747/ to see the Driver UI


# References

This standalone docker-compose was made based on: https://github.com/apache/spark/tree/v3.2.0/external/docker/spark-test
	

