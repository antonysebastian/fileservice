package com.fileservice.service;

import java.io.*;
import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class that encrypts or decrypts a file.
 * @author www.codejava.net
 *
 */
public class CryptoUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoUtil.class);
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private static final int READ_SIZE = 1024 * 10;

    protected static boolean encryptAndWrite(String key, InputStream inputStream, File outputFile) {
        LOGGER.debug("Writing file {}", outputFile.getName());
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            ByteArrayOutputStream fileByteos = new ByteArrayOutputStream();
            //Read file based on READ_SIZE and write it to the byte output stream
            byte[] bytes = new byte[READ_SIZE];
            int read = 0;
            while((read = inputStream.read(bytes)) != -1) {
                fileByteos.write(bytes, 0, read);
            }

            //Encrypt the file
            byte[] encryptedFileBytes = cipher.doFinal(fileByteos.toByteArray());
            //write the encrypted file
            outputStream.write(encryptedFileBytes, 0, encryptedFileBytes.length);

            fileByteos.close();
            inputStream.close();
            outputStream.close();

            LOGGER.debug("File {} written", outputFile.getName());
            return true;
        } catch (Exception e) {
            LOGGER.debug("Could not write file {}", outputFile.getName());
            LOGGER.debug("Error : {}", e);
        }
        return false;
    }

    protected static byte[] decryptAndRead(String key, File inputFile) {
        LOGGER.debug("Returning file {}", inputFile.getName());
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            FileInputStream inputStream = new FileInputStream(inputFile);
            //Read file as bytes
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);
            //Decrypt the file
            byte[] outputBytes = cipher.doFinal(inputBytes);

            inputStream.close();

            LOGGER.debug("Returned file {}", inputFile.getName());
            return outputBytes;
        } catch (Exception e) {
            LOGGER.debug("Could not return file {}", inputFile.getName());
            LOGGER.debug("Error : {}", e);
        }
        return null;
    }

    protected static String generateSecretKey() {
        KeyGenerator keyGen;
        try {
            keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            LOGGER.debug("Could not generate secret key");
            LOGGER.debug("Error : {}", e);
        }
        return null;
    }
}