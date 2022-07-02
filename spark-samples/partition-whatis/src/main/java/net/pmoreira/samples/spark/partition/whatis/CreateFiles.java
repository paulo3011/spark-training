package net.pmoreira.samples.spark.partition.whatis;

import net.pmoreira.samples.spark.utils.FakeCsvWriter;

import java.io.IOException;

public class CreateFiles {
    public static void main(String[] args) throws IOException {
        String csvLine = "Spark performs as many steps as it can at one point in time before writing data to memory or disk.";
        String outputPath = "docker/data/hugefile.csv";
        FakeCsvWriter.createHugeCsvFile(csvLine, outputPath, 55000000); //55000000 = +- 5.192GB in 2m 4s
    }
}
