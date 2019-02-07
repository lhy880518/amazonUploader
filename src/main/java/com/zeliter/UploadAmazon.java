package com.zeliter;


import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import com.zeliter.config.PropertiesReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;
import java.util.Properties;


@Slf4j
public class UploadAmazon {

    public String upload(Map<String, Object> input, Context context) throws IOException, InterruptedException {

        LambdaLogger logger = context.getLogger();
        logger.log("input = "+input);

        String ouuid = (String) input.get("ouuid");
        String base64string = (String) input.get("base64string");

        String bucketName = (String) input.get("bucketName");
        String keyName = input.get("keyName")+ouuid+".png";
        String accessKey = (String) input.get("accessKey");
        String secretKey = (String) input.get("secretKey");

        BufferedImage combined2 =ImageIO.read(new ByteArrayInputStream(Base64.decodeBase64(base64string)));
        ByteArrayOutputStream qrResultOs = new ByteArrayOutputStream();
        ImageIO.write(combined2, "png", qrResultOs);

        TransferManager transferManager = new TransferManager(new BasicAWSCredentials(accessKey, secretKey));
        byte[] md5 = md5(qrResultOs.toByteArray(), qrResultOs.toByteArray().length);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(qrResultOs.toByteArray().length);
        metadata.setLastModified(new Date());
        metadata.setContentMD5(toBase64(md5));
        PutObjectRequest request = new PutObjectRequest(bucketName, keyName, new ByteArrayInputStream(qrResultOs.toByteArray()), metadata);
        /*
        * You can ask the upload for its progress, or you can
        * add a ProgressListener to your request to receive notifications
        * when bytes are transferred.
        */
        request.setGeneralProgressListener(new ProgressListener() {
            @Override
            @SuppressWarnings("synthetic-access")
            public void progressChanged(ProgressEvent progressEvent) {

            }
        });

        /*
        * TransferManager processes all transfers asynchronously, so this call will return immediately.
        */
        Upload upload = transferManager.upload(request);

        // You can block and wait for the upload to finish
        upload.waitForCompletion();

        logger.log("keyName = "+keyName);

        return keyName;
    }

    public String upload2(Map<String, Object> input) throws IOException, InterruptedException {


        PropertiesReader propertiesReader = new PropertiesReader();
        System.out.println(new File(".").getCanonicalPath());

        propertiesReader.loadProp(new File(".").getCanonicalPath()+"/application.properties");
//        Properties properties = propertiesReader.getProperties();
//
//        String ouuid = (String) input.get("ouuid");
//        String base64string = (String) input.get("base64string");

        //return properties.getProperty("bucketName");
        return "test";
    }
    public static String toBase64(byte[] md5)
    {
        byte encoded[] = Base64.encodeBase64(md5, false);
        return new String(encoded);
    }

    public static byte[] md5(byte[] buffer, int length)
    {
        try
        {
            MessageDigest mdigest = MessageDigest.getInstance("MD5");
            mdigest.update(buffer, 0, length);
            return mdigest.digest();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
