package com.fileservice.service;

import java.io.File;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fileservice.cache.DataCache;
import com.fileservice.fileserver.Config;
import com.fileservice.fileserver.FileServiceException;
import com.fileservice.fileserver.FileServiceException.FileServiceError;

public class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    public static final boolean FILE = true;
    public static final boolean DIRECTORY = false;
    public static final String DELIMITTER = "/";

    protected static boolean exists(String path, boolean isFile) {
        File file = new File(path);
        return file.exists() &&
                (isFile ? !file.isDirectory() : file.isDirectory());
    }

    protected static boolean checkRWPermission(String path) {
        File file = new File(path);
        return file.canRead() && file.canWrite();
    }

    protected static boolean checkRPermission(String path) {
        return new File(path).canRead();
    }

    protected static boolean checkWPermission(String path) {
        return new File(path).canWrite();
    }

    public static boolean createDirsIfNotExists(String location) {
        File storageLocation = new File(location);
        LOGGER.debug("Initializing storage configuration");
        if(!storageLocation.exists() || !storageLocation.isDirectory())
            return storageLocation.mkdirs() && checkRPermission(location);
        else
            return checkRWPermission(location);
    }

    protected static boolean upload(InputStream fileInputStream, String fileName) throws FileServiceException {
        //Check Write permission to storage
        String storage = Config.getInstance().getStorageLocation();
        if(!checkWPermission(storage)) {
            LOGGER.debug("No permission to write file {} at {}", fileName, storage);
            throw new FileServiceException(FileServiceError.NO_WRITE_PERMISSION);
        }

        //Update memory cache first
        if(!DataCache.creatIfNotExists(fileName))
        {
            LOGGER.debug("File {} already exists at {}", fileName, storage);
            throw new FileServiceException(FileServiceError.FILE_EXISTS);
        }

        //Generate secret key
        String secretKey = CryptoUtil.generateSecretKey();
        if(null == secretKey) {
            DataCache.files.remove(fileName);
            return false;
        }

        //Encrypt the file and write to the file system
        FileMetadata metadata = DataCache.files.get(fileName);
        metadata.setKey(secretKey);
        String path = storage + DELIMITTER + fileName;
        File encryptedFile = new File(path);
        LOGGER.debug("Writing file {}", fileName);
        if(!CryptoUtil.encryptAndWrite(secretKey, fileInputStream, encryptedFile)) {
            DataCache.files.remove(fileName);
            LOGGER.debug("Could not write file {}", fileName);
            return false;
        }
        LOGGER.debug("File {} written", fileName);

        //insert metadata in DB
        if(metadata.insertToDb())
            return true;
        else {
            encryptedFile.delete();
            DataCache.files.remove(fileName);
            LOGGER.debug("Could not write metadata of file {} to DB", fileName);
        }
        return false;
    }

    protected static byte[] download(String fileName) throws FileServiceException {
        //Check in memory cache first
        if(!DataCache.files.containsKey(fileName)) {
            LOGGER.debug("File {} does not exist", fileName);
            throw new FileServiceException(FileServiceError.FILE_NOT_FOUND);
        }

        //Check read permission
        String storage = Config.getInstance().getStorageLocation();
        String path = storage + DELIMITTER + fileName;
        if(!checkRPermission(path)) {
            LOGGER.debug("No permission to read file {} at {}", fileName, storage);
            throw new FileServiceException(FileServiceError.NO_READ_PERMISSION);
        }

        //Decrypt file and return
        FileMetadata metadata = DataCache.files.get(fileName);
        File encryptedFile = new File(path);
        LOGGER.debug("Returning file {}", fileName);
        byte[] decryptedFile = CryptoUtil.decryptAndRead(metadata.getKey(), encryptedFile);
        if(null == decryptedFile) {
            LOGGER.debug("Could not return file {}", fileName);
            return null;
        }
        LOGGER.debug("Returned file {}", fileName);
        return decryptedFile;
    }

    protected static boolean delete(String fileName) throws FileServiceException {
        //Check in memory cache first
        if(!DataCache.files.containsKey(fileName)) {
            LOGGER.debug("File {} does not exist", fileName);
            throw new FileServiceException(FileServiceError.FILE_NOT_FOUND);
        }

        //Check write permission
        String storage = Config.getInstance().getStorageLocation();
        String path = storage + DELIMITTER + fileName;
        if(!checkWPermission(path)) {
            LOGGER.debug("No permission to delete file {} at {}", fileName, storage);
            throw new FileServiceException(FileServiceError.NO_WRITE_PERMISSION);
        }

        //Delete file from file system and metadata from DB
        FileMetadata metadata = DataCache.files.get(fileName);
        if(metadata.deleteFromDb()) {
            DataCache.files.remove(fileName);
            return new File(path).delete();
        }
        else
            LOGGER.debug("Could not delete metadata of file {} from DB", fileName);
        return false;
    }
}
