package com.fileservice.service;

import java.sql.Connection;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fileservice.cache.DBManager;
import com.fileservice.fileserver.Config;

public class FileMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileMetadata.class);
    private String path;
    private String fileName;
    private String key;

    public FileMetadata(String path, String fileName, String key) {
        this.path = path;
        this.fileName = fileName;
        this.key = key;
    }

    public FileMetadata(String path, String fileName) {
        this.path = path;
        this.fileName = fileName;
    }

    //We do not need unitialized objects
    private FileMetadata() {

    }

    protected boolean insertToDb() {
        try(Connection con = DBManager.getInstance().getConnection();
                Statement stmt = con.createStatement();) {
            stmt.executeUpdate("INSERT into `files` VALUES(\"" + path + "\", \"" + fileName + "\", \"" + key + "\"" + ")");
            return true;
        } catch (Exception e) {
            LOGGER.debug("Failed to insert data to DB");
            LOGGER.debug("Error : {}", e);
            return false;
        }
    }

    protected boolean deleteFromDb() {
        try(Connection con = DBManager.getInstance().getConnection();
                Statement stmt = con.createStatement();) {
            stmt.executeUpdate("DELETE FROM `files` WHERE `path` = \"" + Config.getInstance().getStorageLocation()
                    + "\" AND `file_name` = \"" + fileName + "\"");
            return true;
        } catch (Exception e) {
            LOGGER.debug("Failed to delete data from DB");
            LOGGER.debug("Error : {}", e);
            return false;
        }
    }

    protected void setKey(String key) {
        this.key = key;
    }

    protected String getPath() {
        return path;
    }

    protected String getFileName() {
        return fileName;
    }

    protected String getKey() {
        return key;
    }

}

