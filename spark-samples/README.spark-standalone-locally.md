# Install spark 3.2.1 on local ubuntu

```shell
# go to tmp dir to download spark
cd /tmp/
# download spark
wget https://downloads.apache.org/spark/spark-3.2.1/spark-3.2.1-bin-hadoop3.2.tgz
# check if download finished ok (+- 300 MB)
file spark-3.2.1-bin-hadoop3.2.tgz
# should see: spark-3.2.1-bin-hadoop3.2.tgz: gzip compressed data...
# Extract the Spark tarball.
tar xvf spark-3.2.1-bin-hadoop3.2.tgz
# Move spark dir to destination folder
sudo mv spark-3.2.1-bin-hadoop3.2/ /opt/spark
# check if instalation is ok
/opt/spark/bin/spark-submit --version
# Welcome to
#      ____              __
#     / __/__  ___ _____/ /__
#    _\ \/ _ \/ _ `/ __/  '_/
#   /___/ .__/\_,_/_/ /_/\_\   version 3.2.1
#      /_/
```
## Set Spark environment

Open your bashrc configuration file and add:

```shell
# use vim or nano to open bashrc file
nano ~/.bashrc
```
add on .bashrc: 

```shell
export SPARK_HOME=/opt/spark
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin
```

Activate the changes:

```shell
source ~/.bashrc
# check spark submit command line version
spark-submit --version
```

## Starting a standalone master and work services localy

https://spark.apache.org/docs/latest/spark-standalone.html#Executors%20Scheduling

```shell
cd /opt/spark/
export SPARK_MASTER_HOST=localhost
export SPARK_MASTER_PORT=7077
export SPARK_MASTER_WEBUI_PORT=8087
./sbin/start-master.sh
```

Check service

```shell
sudo ss -tunelp | grep 8087
# tcp    LISTEN  0       1                         *:8087                 *:*      users:(("java",pid=30244,fd=294)) uid:1000 ino:4743475 sk:5 v6only:0 <->
# ./bin/spark-shell --master spark://localhost:7077
```

Starting work:

```shell
# if you want to change worker web-ui port default 8081
export SPARK_WORKER_WEBUI_PORT=8081
./sbin/start-worker.sh spark://localhost:7077
```

Run sample spark app on spark-shell:

```shell
/opt/spark/bin/spark-shell
# to quit:
:quit
```

Runinig a sample

```shell
/opt/spark/bin/run-example SparkPi 10
```

## Stop services:

```shell
cd /opt/spark/
# Stops both the master and the workers as described above.
./sbin/stop-all.sh
```



