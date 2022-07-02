package net.pmoreira.samples.spark.partition.groupbykey;

import net.pmoreira.samples.spark.utils.FakeCsvWriter;

import java.io.IOException;

public class CreateFiles {
    public static void main(String[] args) throws IOException {
        /*
        Emulate one file with about +- 800 MB (csv) with:
        - data for two groups of key (diviceID 1 with 600 GB and diviceID 2 with 200 MB)
         */
        String fixedColumn = "Spark performs as many steps as it can at one point in time before writing data to memory or disk.";
        String deviceId1 = "divice-01," + fixedColumn;
        String deviceId2 = "divice-02," + fixedColumn;
        String file1 = "docker/data/device-01.csv";
        String file2 = "docker/data/device-02.csv";
        String file3 = "docker/data/all-devices.csv";
        int totalLineFor1GB = 9900000;
        int totalLineFor1MB = 9900;
        FakeCsvWriter.createHugeCsvFile(deviceId1, file1, totalLineFor1MB * 600); // => 9900 * 600 = 5940000 lines
        FakeCsvWriter.createHugeCsvFile(deviceId2, file2, totalLineFor1MB * 200); // 9900 * 200 = 1980000
        String[] files = new String[] {file1, file2};
        FakeCsvWriter.concatenateFiles(files,file3); // => 1980000 + 5940000 = 7920000 (800 MB)
    }
}
