package com.fileservice.fileserver;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fileservice.cache.DBManager;
import com.fileservice.cache.DataCache;
import com.fileservice.service.FileUtil;

/**
 * Main class.
 *
 */
public class Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    // Base URI the Grizzly HTTP server will listen on
    private static final String BASE_URI = "http://%s:%d/";
    private static HttpServer server;

    private static HttpServer createServer(Config config) {
        LOGGER.debug("Initializing data cache");
        if(!DataCache.initialize())
        {
            LOGGER.error("Could not initialize cache");
            System.exit(1);
        }

        // create a resource config that scans for JAX-RS resources and providers
        // in com.oracle.fileService package
        final ResourceConfig rc = new ResourceConfig().packages("com.fileservice.service");
        //Registering Auth Filter
        rc.register(com.fileservice.service.BasicAuthFilter.class);
        //Registering role based filtering
        rc.register(RolesAllowedDynamicFeature.class);
        //Registering multipart format
        rc.register(MultiPartFeature.class);

        //create
        String baseUri = String.format(BASE_URI, Config.getInstance().getHostName(), Config.getInstance().getPort());
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc, false);
    }

    public static void main(String[] args) throws IOException {
        //Initialize config
        Config config = Config.getInstance();
        //Validate Configiration
        //Check if storage loaction is accessible with required permissions
        if(null == config || !validate(config))
        {
            LOGGER.error("Could not load configuration settings");
            System.exit(1);
        }
        LOGGER.debug("Starting server with the following configuration");
        LOGGER.debug("hostname {}", config.getHostName());
        LOGGER.debug("port {}", config.getPort());
        LOGGER.debug("storage {}", config.getStorageLocation());
        LOGGER.debug("DB Host {}", config.getDbHost());
        LOGGER.debug("DB Port {}", config.getDbPort());
        LOGGER.debug("DB Name {}", config.getDbName());
        LOGGER.debug("DB Initial Connection Pool Size {}", config.getDbInitialSize());
        LOGGER.debug("DB Connection Pool Maximum Size {}", config.getDbMaxTotal());
        LOGGER.debug("DB Connection Connection Pool Max Idle Size {}", config.getDbMaxIdle());
        LOGGER.debug("DB Connection Connection Pool Min Idle Size {}", config.getDbMinIdle());
        LOGGER.debug("DB Initial Connection Pool Eviction Time To Connection Pool Min Idle Size {}",
                config.getDbEvictionTimeToMinIdle());
        LOGGER.debug("DB Initial Connection Pool Eviction Time From Connection Pool Min Idle Size {}",
                config.getDbEvictionTimeFromMinIdle());
        server = createServer(config);
        int status = 0;
        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
            //Selector thread pool config
            ThreadPoolConfig selectorPoolConfig = ThreadPoolConfig.defaultConfig().
                    setPoolName("selector-pool").
                    setCorePoolSize(config.getSelectorCoreSize()).
                    setMaxPoolSize(config.getSelectorMaxSize()).
                    setQueueLimit(config.getSelectorQueueLimit()).
                    setKeepAliveTime(config.getSelectorEvictionTime(), TimeUnit.SECONDS);

            //Worker thread pool config
            ThreadPoolConfig workerPoolConfig = ThreadPoolConfig.defaultConfig().
                    setPoolName("worker-pool").
                    setCorePoolSize(config.getWorkerCoreSize()).
                    setMaxPoolSize(config.getWorkerMaxSize()).
                    setQueueLimit(config.getWorkerQueueLimit()).
                    setKeepAliveTime(config.getWorkerEvictionTime(), TimeUnit.SECONDS);

            //assign the thread pool
            NetworkListener listener = server.getListener("grizzly");
            TCPNIOTransport transport = listener.getTransport();
            transport.setKernelThreadPoolConfig(selectorPoolConfig);
            transport.setWorkerThreadPoolConfig(workerPoolConfig);

            //Start Http server
            server.start();
            LOGGER.debug("Server started");
            Thread.currentThread().join();
        } catch (Exception e) {
            LOGGER.error("Could not start server");
            LOGGER.debug("Error : {}", e);
            status = 1;
        }
        System.exit(status);
    }

    private static boolean isPortOccupied(int port) {
        ServerSocket s = null;
        try {
            if (port > 0)
                s = new ServerSocket(port);
            else
            {
                LOGGER.error("Port cant be a negative number");
                return true;
            }
        } catch (IOException e) {
            LOGGER.error("Could not open port {}; error : {}. Maybe Server is already running?", port, e.getMessage());
            return true;
        } catch (Exception e) {
            LOGGER.error("Error opening port {}; error : {}", port, e.getMessage());
            return true;
        } finally {
            if (s != null)
                try {
                    s.close();
                } catch (IOException e) {
                }
        }
        return false;
    }

    private static boolean validate(Config config) {
        return !isPortOccupied(config.getPort()) && FileUtil.createDirsIfNotExists(config.getStorageLocation());
    }

    private static class ShutdownHook extends Thread {
        public ShutdownHook() {
            super("ShutdownHook");
        }

        @Override
        public void run() {
            LOGGER.debug("Shutting down server");
            try {
                //Destroying db connection pool
                DBManager.getInstance().close();
                server.shutdown();
            } catch (IOException | SQLException | PropertyVetoException e) {
                LOGGER.error("Could not destroy connection pool");
            }
            LOGGER.debug("Server stopped.");
        }
    }
}
