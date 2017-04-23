package service.provider.avito;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.Play;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.javacpp.lept.pixDestroy;

@Singleton
public class AvitoPhoneParser implements AutoCloseable {

    private Logger logger = LoggerFactory.getLogger(AvitoPhoneParser.class);

    private tesseract.TessBaseAPI api;

    @PostConstruct
    public void init() throws IOException {
        InputStream trainedData = Play.current().classloader().getResourceAsStream("tesseract/tessdata/eng.traineddata");
        byte[] tessTrainedData = IOUtils.toByteArray(trainedData);
        File tmpTrainedDataFile = new File("/tmp/tesseract/tessdata/eng.traineddata");
        FileUtils.forceMkdirParent(tmpTrainedDataFile);
        FileUtils.writeByteArrayToFile(tmpTrainedDataFile, tessTrainedData);

        api = new tesseract.TessBaseAPI();
        if (api.Init(new BytePointer("/tmp/tesseract"), new BytePointer("eng")) != 0) {
            throw new IllegalStateException("Couldn't initDriver tesseract");
        }
        api.SetVariable("tessedit_char_whitelist", "0123456789-");
    }

    public long parseNumbersFromImage(String dataImagePhoto) {
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

    @Override
    public void close() throws Exception {
        api.End();
    }

}