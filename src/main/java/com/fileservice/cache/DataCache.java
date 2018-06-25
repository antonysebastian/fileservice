package com.fileservice.cache;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fileservice.fileserver.Config;
import com.fileservice.service.FileMetadata;


public class DataCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataCache.class);
    public static Map<String, FileMetadata> files = new HashMap<String, FileMetadata>();

    private DataCache() {

    }

    public static boolean initialize() {
        return initializeCache("files");
    }

    private static boolean initializeCache(String cacheName) {
        String storage = Config.getInstance().getStorageLocation();
        String query = "SELECT * FROM `files` " + "WHERE `path` = \"" + storage + "\"";
        LOGGER.debug("DB HIT : {}", query);
        try(Connection con = DBManager.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query)) {
            while (result.next()) {
                files.put(result.getString("file_name"), new FileMetadata(storage,
                        result.getString("file_name"), result.getString("encryption_key")));
            }
            LOGGER.debug("Initialized data cache");
            return true;
        } catch (Exception e) {
            LOGGER.error("Could not initailize data cache {}", cacheName);
            LOGGER.error("Error : {}", e);
        }
        return false;
    }

    synchronized public static boolean creatIfNotExists(String fileName) {
        if(files.containsKey(fileName)) {
            return false;
        }
        files.put(fileName, new FileMetadata(Config.getInstance().getStorageLocation(), fileName));
        return true;
    }

    public static String authenticate(String user, String encryptedPass) {
        String query = "SELECT * FROM `users` " + "WHERE `login` = \"" + user + "\"";
        LOGGER.debug("DB HIT : {}", query);
        try(Connection con = DBManager.getInstance().getConnection();
                Statement stmt = con.createStatement();
                ResultSet result = stmt.executeQuery(query)) {
            while (result.next()) {
                if(result.getString("password").equals(encryptedPass))
                    return result.getString("role");
            }
        } catch (Exception e) {
            LOGGER.debug("Could not authenticate user {}", user);
            LOGGER.debug("Error : {}", e);
        }
        return null;
    }
}
