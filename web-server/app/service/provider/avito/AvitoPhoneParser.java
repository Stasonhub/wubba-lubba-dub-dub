package service.provider.avito;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Play;

import java.io.File;
import java.io.InputStream;

import static org.bytedeco.javacpp.lept.pixDestroy;

class AvitoPhoneParser {

    private Logger logger = LoggerFactory.getLogger(AvitoPhoneParser.class);

    private volatile tesseract.TessBaseAPI api;

    private void init() {
        if (api == null) {
            synchronized (this) {
                if (api == null) {
                    try {
                        InputStream trainedData = Play.current().classloader().getResourceAsStream("tesseract/tessdata/eng.traineddata");
                        byte[] tessTrainedData = IOUtils.toByteArray(trainedData);
                        File tmpTrainedDataFile = new File("/tmp/tesseract/tessdata/eng.traineddata");
                        FileUtils.forceMkdirParent(tmpTrainedDataFile);
                        FileUtils.writeByteArrayToFile(tmpTrainedDataFile, tessTrainedData);

                        tesseract.TessBaseAPI localApi = new tesseract.TessBaseAPI();
                        if (localApi.Init(new BytePointer("/tmp/tesseract"), new BytePointer("eng")) != 0) {
                            throw new IllegalStateException("Couldn't initDriver tesseract");
                        }
                        localApi.SetVariable("tessedit_char_whitelist", "0123456789-");

                        api = localApi;
                    } catch (Exception e) {
                        logger.error("Failed to init phone parser", e);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public long parseNumbersFromImage(String dataImagePhoto) {
        init();

        String base64Image = dataImagePhoto.split(",")[1];

        long startTime = System.currentTimeMillis();
        logger.trace("Parsing image {} ", dataImagePhoto);

        byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);

        lept.PIX image = lept.pixReadMemPng(imageBytes, imageBytes.length);
        api.SetImage(image);
        BytePointer outText = api.GetUTF8Text();
        String result = outText.getString();
        outText.deallocate();
        pixDestroy(image);

        logger.trace("Parsing image completed in {} s", (System.currentTimeMillis() - startTime) / 1000);

        String numbers = result.replaceAll("[^\\d]", "");
        if (numbers.length() != 11 || numbers.charAt(0) != '8') {
            throw new IllegalArgumentException("Recognition failure. Received " + result + " for " + dataImagePhoto);
        }
        return Long.parseLong(numbers.substring(1));
    }

    public void close() {
        if (api != null) {
            api.End();
        }
    }

}