package com.fileservice.fileserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config{

    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private String storage;
    private String hostname;
    private int port;
    private String dbHost;
    private int dbPort;
    private String dbName;
    private String dbPass;
    private String dbUsername;
    private int dbInitialSize;
    private int dbMaxTotal;
    private int dbMaxIdle;
    private int dbMinIdle;
    private long dbEvictionTimeToMinIdle;
    private long dbEvictionTimeFromMinIdle;
    private int selectorCoreSize;
    private int selectorMaxSize;
    private int selectorQueueLimit;
    private long selectorEvictionTime;
    private int workerCoreSize;
    private int workerMaxSize;
    private int workerQueueLimit;
    private long workerEvictionTime;



    private static Config instance = null;

    private Config () {

    }

    public static Config getInstance() {
        if(null == instance) {
            instance = new Config();
            if(!instance.load(System.getProperty("user.dir") + "/" + "config.properties"))
                instance = null;
        }
        return instance;
    }

    protected boolean load(String configFile) {
        LOGGER.debug("Loading configuration from file {}", configFile);
        Properties properties = new Properties();
        try {
            properties.load(new BufferedReader(new FileReader(configFile)));
            hostname = properties.getProperty("hostname", "localhost").trim();
            storage = properties.getProperty("storage_location", System.getProperty("user.dir") + "/" + "files").trim();
            port = getIntProperty(properties, "port", 7701);
            dbHost = properties.getProperty("db_host", "localhost").trim();
            dbPort = getIntProperty(properties, "db_port", 3306);
            dbName = properties.getProperty("db_name", "file_service").trim();
            dbPass = properties.getProperty("db_pass", "123").trim();
            dbUsername = properties.getProperty("db_user", "root").trim();
            dbInitialSize = getIntProperty(properties, "initial_size", 2);
            dbMaxTotal = getIntProperty(properties, "max_total", 100);
            dbMaxIdle = getIntProperty(properties, "max_idle", 10);
            dbMinIdle = getIntProperty(properties, "min_idle", 3);
            dbEvictionTimeToMinIdle = getLongProperty(properties, "eviction_time_to_min_idle", 1800000);
            dbEvictionTimeFromMinIdle = getLongProperty(properties, "eviction_time_from_min_idle", 21600000);
            selectorCoreSize = getIntProperty(properties, "selector_core_pool_size", 2);
            selectorMaxSize = getIntProperty(properties, "selector_max_pool_size", 10);
            selectorQueueLimit = getIntProperty(properties, "selector_queue_limit", 40);
            selectorEvictionTime = getLongProperty(properties, "selector_keep_alive_time", 21600000);
            workerCoreSize = getIntProperty(properties, "worker_core_pool_size", 5);
            workerMaxSize = getIntProperty(properties, "worker_max_pool_size", 20);
            workerQueueLimit = getIntProperty(properties, "worker_queue_limit", 40);
            workerEvictionTime = getLongProperty(properties, "worker_keep_alive_time", 1800000);
        } catch (Exception e) {
            LOGGER.error("Could not read properties from file {}", configFile);
            LOGGER.debug("Error : {}", e);
            return false;
        }
        return true;
    }

    private int getIntProperty(Properties properties, String property, int defaultValue) {
        if(properties.getProperty(property) == null)
            return defaultValue;
        else {
            try {
                return Integer.parseInt(properties.getProperty(property).trim());
            } catch (Exception e) {
                LOGGER.error("Cant read interger property {} from config file", property);
                LOGGER.debug("Error : {}", e);
            }
        }
        return defaultValue;
    }

    private long getLongProperty(Properties properties, String property, long defaultValue) {
        if(properties.getProperty(property) == null)
            return defaultValue;
        else {
            try {
                return Long.parseLong(properties.getProperty(property).trim());
            } catch (Exception e) {
                LOGGER.error("Cant read interger property {} from config file", property);
                LOGGER.debug("Error : {}", e);
            }
        }
        return defaultValue;
    }

    public String getHostName() {
        return hostname;
    }

    public String getStorageLocation() {
        return storage;
    }

    public int getPort() {
        return port;
    }

    public String getDbHost() {
        return dbHost;
    }

    public int getDbPort() {
        return dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPass() {
        return dbPass;
    }

    public int getDbInitialSize() {
        return dbInitialSize;
    }

    public int getDbMaxTotal() {
        return dbMaxTotal;
    }

    public int getDbMaxIdle() {
        return dbMaxIdle;
    }

    public int getDbMinIdle() {
        return dbMinIdle;
    }

    public long getDbEvictionTimeToMinIdle() {
        return dbEvictionTimeToMinIdle;
    }

    public long getDbEvictionTimeFromMinIdle() {
        return dbEvictionTimeFromMinIdle;
    }

    public int getSelectorCoreSize() {
        return selectorCoreSize;
    }

    public int getSelectorMaxSize() {
        return selectorMaxSize;
    }

    public int getSelectorQueueLimit() {
        return selectorQueueLimit;
    }

    public long getSelectorEvictionTime() {
        return selectorEvictionTime;
    }

    public int getWorkerCoreSize() {
        return workerCoreSize;
    }

    public int getWorkerMaxSize() {
        return workerMaxSize;
    }

    public int getWorkerQueueLimit() {
        return workerQueueLimit;
    }

    public long getWorkerEvictionTime() {
        return workerEvictionTime;
    }

}
