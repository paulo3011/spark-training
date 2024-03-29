#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM ubuntu:20.04

# Upgrade package index
# install a few other useful packages plus Open Java 11
# docker image size (by ~30MB)
RUN apt-get update && \
    apt-get install -y less openjdk-11-jre-headless iproute2 vim-tiny sudo openssh-server   

# download and install spark
RUN apt-get install -y curl
RUN apt-get install -y tar
RUN apt-get install -y gzip

# Remove unneeded /var/lib/apt/lists/* after install to reduce the image size
RUN rm -rf /var/lib/apt/lists/*

# The WORKDIR instruction sets the working directory for any RUN, CMD, ENTRYPOINT, COPY and ADD instructions that follow it in the Dockerfile.
# https://docs.docker.com/engine/reference/builder/#workdir
WORKDIR /tmp/spark

# download spark
# http://www.compciv.org/recipes/cli/downloading-with-curl/
RUN curl https://downloads.apache.org/spark/spark-3.2.1/spark-3.2.1-bin-hadoop3.2.tgz --output spark-3.2.1-bin-hadoop3.2.tgz
# if download finished ok (+- 300 MB)
# should see: spark-3.2.1-bin-hadoop3.2.tgz: gzip compressed data...
# Extract the Spark tarball.
# tar -xf archive.tar -> Extract all files from archive.tar.
RUN tar -xvf spark-3.2.1-bin-hadoop3.2.tgz
# Move spark dir to destination folder
RUN mv spark-3.2.1-bin-hadoop3.2 /opt/spark
# delete gzip file
RUN rm spark-3.2.1-bin-hadoop3.2.tgz

# ENV values are available to containers, but also RUN-style commands during the Docker build starting with the line where they are introduced.
# If you set an environment variable in an intermediate container using bash (RUN export VARI=5 && …) it will not persist in the next command.
# https://vsupalov.com/docker-arg-env-variable-guide/

ENV SPARK_HOME=/opt/spark
# Put the SPARK_HOME in the machine path so that we can run spark
ENV PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin

WORKDIR /opt/spark

# https://spark.apache.org/docs/latest/spark-standalone.html#monitoring-and-logging
# Detailed log output for each job is also written to the work directory of each worker node (SPARK_HOME/work by default).
# You will see two files for each job, stdout and stderr, with all output it wrote to its console
# create dirs for custom logs
RUN mkdir logs && mkdir ./logs/heap-dumps

# Directory to use for "scratch" space in Spark, including map output files and RDDs that get stored on disk.
# This should be on a fast, local disk in your system. It can also be a comma-separated list of multiple directories on different disks.
# ENV SPARK_LOCAL_DIRS
# Directory to run applications in, which will include both logs and scratch space (default: SPARK_HOME/work).
# ENV SPARK_WORKER_DIR

# A nice image of default spark ports: https://www.ibm.com/docs/en/SSCTFE_1.1.0/com.ibm.azk.v1r1.azka100/images/spark-ports.gif

# Ports for standalone mode

# https://spark.apache.org/docs/latest/spark-standalone.html#installing-spark-standalone-to-a-cluster
# The master and each worker has its own web UI that shows cluster and job statistics.
# Port for web UI (default: 8080 for master, 8081 for worker)
# We can change master web ui through SPARK_MASTER_WEBUI_PORT env
# We can change worker web ui through SPARK_WORKER_WEBUI_PORT env
EXPOSE 8080 8081
# Port for service to listen on (default: 7077 for master, random for worker)
# We can change worker port through SPARK_WORKER_PORT env
# We can change master port through SPARK_MASTER_PORT env (default: 7077)
ENV SPARK_MASTER_PORT 7077
EXPOSE 7077
ENV SPARK_WORKER_PORT 8888
EXPOSE 8888
# https://spark.apache.org/docs/3.2.1/monitoring.html
# The application web UI at http://<driver>:4040 - your application's dashboard, which shows memory and workload data. Can be swap out through spark.ui.port
# If multiple SparkContexts are running on the same host, they will bind to successive ports beginning with 4040 (4041, 4042, etc).
EXPOSE 4040 4041
# history server - 18080 (listing incomplete and completed applications and attempts). https://spark.apache.org/docs/3.2.1/monitoring.html
# You can start the history server by executing: ./sbin/start-history-server.sh
EXPOSE 18080