package net.pmoreira.samples.spark.partition.groupbykey;

import net.pmoreira.samples.spark.utils.FakeCsvWriter;

import java.io.IOException;

public class CreateFiles {
    public static void main(String[] args) throws IOException {
        FakeCsvWriter.createHugeCsvFile("1,devicea","docker/data/tinyfile.csv", 5);
    }
}
