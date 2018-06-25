package com.fileservice.cache;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fileservice.fileserver.Config;

public class DBManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBManager.class);
    private static DBManager cache = null;
    private BasicDataSource dataSource = null;

    private DBManager() throws IOException, SQLException, PropertyVetoException {
        Config config = Config.getInstance();
        dataSource = new BasicDataSource();
        dataSource.setUsername(config.getDbUsername());
        dataSource.setPassword(config.getDbPass());
        dataSource.setUrl("jdbc:mysql://" + config.getDbHost() + ":" + config.getDbPort() + "/" + config.getDbName() + "?serverTimezone=UTC");

        //dbcp tuning
        dataSource.setInitialSize(config.getDbInitialSize());
        dataSource.setMaxTotal(config.getDbMaxTotal());
        dataSource.setMaxIdle(config.getDbMaxIdle());
        dataSource.setMinIdle(config.getDbMinIdle());
        dataSource.setMinEvictableIdleTimeMillis(config.getDbEvictionTimeToMinIdle());
        dataSource.setSoftMinEvictableIdleTimeMillis(config.getDbEvictionTimeFromMinIdle());
    }

    public static DBManager getInstance() throws IOException, SQLException, PropertyVetoException {
        if (cache == null) {
            cache = new DBManager();
            return cache;
        } else {
            return cache;
        }
    }

    public Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    public void close() {
        LOGGER.debug("Closing connection pool");
        try {
            dataSource.close();
        } catch (SQLException e) {
            LOGGER.error("Could not close connection pool");
        }
    }

}