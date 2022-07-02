package net.pmoreira.samples.spark.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FakeCsvWriter {

    public static void createHugeCsvFile(String outputPath) throws IOException {
        String csvLine = "Spark performs as many steps as it can at one point in time before writing data to memory or disk.";
        createHugeCsvFile(csvLine, outputPath, 55000000); //55000000 = +- 5.192GB in 2m 4s
    }

    /**
     *
     * @param lineToRepeat
     * @param outputPath
     * @param numberOfLines
     * @throws IOException
     * @see <a href="https://www.amitph.com/java-read-write-large-files-efficiently/">Using FileChannel</a>
     * @see <a href="https://stackoverflow.com/questions/1062113/fastest-way-to-write-huge-data-in-text-file-java">Fastest way to write huge data in text file Java</a>
     */
    public static void createHugeCsvFile(String lineToRepeat, String outputPath, int numberOfLines) throws IOException {
        byte[] buffer = String.format("%s\n", lineToRepeat).getBytes();

        final int recordSize = buffer.length;
        final long totalSize = recordSize * numberOfLines;
        final double mega = (Math.pow(1024, 2));
        System.out.println("will write one file with: " + totalSize / mega + " MB or " + totalSize + " bytes");
        System.out.println("each line has: " + recordSize / mega + " MB or " + recordSize + " bytes");
        System.out.println("1MB = " + mega);

        try(FileChannel outputChannel = new FileOutputStream(outputPath).getChannel())
        {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(recordSize);
            for (int i = 0; i < numberOfLines; i++) {
                byteBuffer.put(buffer);
                byteBuffer.flip();
                outputChannel.write(byteBuffer);
                byteBuffer.clear();
            }
        }

    }
}
