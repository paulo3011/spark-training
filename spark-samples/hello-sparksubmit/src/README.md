# AWS EMR Serverless Sample run

````shell
aws emr-serverless start-job-run \
    --application-id 00f7bgsr28hsu109 \
    --execution-role-arn arn:aws:iam::852046719065:role/emr-serverless-job-role \
    --job-driver '{
        "sparkSubmit": {
            "entryPoint": "s3://moreira-poc/emrserverless/spark/hello-sparksubmit/hello-sparksubmit-all.jar",
            "entryPointArguments": ["fakearg"],
            "sparkSubmitParameters": "--conf spark.executor.cores=1 --conf spark.executor.memory=1g --conf spark.driver.cores=1 --conf spark.driver.memory=1g --conf spark.executor.instances=1"
        }
    }'
````

Seealso: https://docs.aws.amazon.com/emr/latest/EMR-Serverless-UserGuide/jobs-spark.html#spark-defaults