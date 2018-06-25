# fileservice

A java HTTP file service based on REST standards to store, retrieve and delete files

# Description

This is an asynchronous server designed to store, retrieve and delete files. This HTTP REST server is designed as an asynchronous server and can handle a very high number of concurrent requests. It supports Basic Authentication and has supports privilages so that all requests can be validated. Also, all the files stored are encrypted and are decrypted when retrieved.

# Pre Requisites

- JDK 1.8
- Appache Maven - 3.3.3 or above
- MySQL Community Server - 5.6.24 or above

# External Libraries

- This project utlizes Grizzly as its web server and Jersey for marshalling/unmarshalling.
- The libraries used for connection pooling are appache-commons-dbcp2 and appache-commons-pool2.
- Logging is handled by log4j and slf4j

# Usage

## Actions

There are 3 actions that can be performed -
- READ - Download the file
- WRITE - Upload the file
- DELETE - Delete the file. This could be of use when a file is being replaced by another one

## Authentication

The application can be accessed only using a username and password. There is a basic authentication performed for all actions.
The auth string is the base 64 encoded value of username:password

## Request Formats

- WRITE

    Method - POST
    Url - http://<host>:<port>/fileService/files
    Headers -
        Authorization - base 64 encoded value of username:password
    Body - Body should be multi-part form data with the following key value pair
        file - file to be uploaded

- READ

    Method - GET
    Url - http://<host>:<post>/fileService/files/{filename}
    Headers -
        Authorization - base 64 encoded value of username:password

- DELETE

    Method - DELETE
    Url - http://<host>:<port>/fileService/files/{filename}
    Headers -
        Authorization - base 64 encoded value of username:password

## Response Format

  - SUCCESS - A response code 200 will be returned with the following body.

    Response Code - 200
    Data - A json string of the format {"OK":{"Operation":"WRITE/READ/DELETE","Resource": "fileName"}}

  - ERROR - All response error codes like 400, 404, 403 etc can be returned by the system. In addition to this, errors that happen during the execution of logic are returned in the following format

    Response Code - 200
    Data - A json string of the format {"Error":{"Code":ErrorCode(int),"Description":"Description of the error"}}

    The following are the codes and description of errors used in the application

    1. "The requested operation could not be done." - When some unexpected error happens in the system
    2. "File not found." - File is not found, for READ and DELETE action
    3. "No read permission." - No read permission to the file, is very rare and occurs only if application doesnt have access to the file
    4. "No write permission. - Same as 3, but for write
    5. "File already exists" - File already exists, for WRITE action
    6. "User is unauthorized" - This is generally not thrown as the server automatically throws a 401 on unauthorized access
    7. "Mandatory parameter(s) missing" - For WRITE operation, when the parameter 'file' is passed wrongly

## Authorization

There are 2 user types supported currently and this is used to restrict the actions that can be performed by the user type.

- Admin - This user type has permission to READ, WRITE, DELETE files from the system.

- User - This user type has permission to READ files from the system.

# Packaging

Execute the following command from the project directory to build and package the project

`mvn package`

After executing this a new file named `file-service-<version>.tar.gz` will be generated.

# Configuration

Using the `file-service-<version>.tar.gz` file generated in the previous step, execute the following command

1. `tar -xvf file-service-<version>.tar.gz`

This will generate a new folder with the name `file-service-<version>`

2. cd `file-service-<version>`

3. Use the `config.properties` and `log-config.xml` files to configure the application and logging respectively. A detailed discription on the this configuration is given below.

4. Start the service using the command `./file_service.sh start`. (Make sure the file `file_service.sh` has execute permission. If not change the permission using `chmod` command)

After this step, the server should be up and running

5. To stop the server, execute `./file_service.sh stop`

# config.properties

The `config.proerties` file that is present inside the `file-service-<version>` is used for configuring the server. The following are the properties the file supports

1. hostname
    - hostname at which the service is running
    - default value is `localhost`

2. port
    - Port which the service uses
    - dfault value is `7701`

3. storage_location
    - Path to storage of files. This is the location where files would be saved and retrieved from. Make sure file permissions are enabled at this path
    - default value is `/path/to/storage/location`

4. db_host
    - Host of the Mysql server
    - default value is `localhost`

5.  db_port
    - Port of the Msql server
    - default value is `3306`
    
6. db_name
    - Name of the db to store file metadata
    - default value is `file_service`
    
7. db_user
    - Name of the user as which the server connects to the db
    - default value is `root`
    
8. db_pass
    - Database Password
    = default value is `123`

9. initial_size
    - Initial number of connections to be created in the db connection pool
    - default value is `2`

10. max_total
    - Maximum connections that can be simultaneously made by the connection pool
    - default value is `30`

11. max_idle
    - Maximum connections in the pool that can stay idle
    - default value is `5`

12. min_idle
    - Minimum connections in the pool that will be maintained
    - default value is `3`

13. eviction_time_to_min_idle
    - Time in seconds after which the connections in the pool will be reduced to the `min_idle` value. Set value '-1' for no eviction
    - default value is `1800000`

14. eviction_time_from_min_idle
    - Time in seconds after which the connections in the pool will be reduced to the 0 after reaching `min_idle`. Set value `-1` for no eviction
    - default value is `21600000`

15. selector_core_pool_size
    - Initial size of the selector thread pool
    - default value is `2`
    
16. selector_max_pool_size
    - Maximum size of selector thread pool
    - default value is `10`
    
17. selector_queue_limit
    - The maximum number of requests that can be queued up in the selector pool.
    - default value is `40`
    
18. selector_keep_alive_time
    - The time for which each thread in the selector pool is kept alive after it is free
    - defvault value is `21600000`
    
15. worker_core_pool_size
    - Initial size of the worker thread pool
    - default value is `5`
    
16. worker_max_pool_size
    - Maximum size of worker thread pool
    - default value is `20`
    
17. worker_queue_limit
    - The maximum number of requests that can be queued up in the worker pool.
    - default value is `40`
    
18. worker_keep_alive_time
    - The time for which each thread in the worker pool is kept alive after it is free
    - defvault value is `1800000`

# Logging

The logging is done to a file named file-service.log and logs for each day are automatically separated. All slf4j log levels are supported. The log levels for each package can be set in the `log-config.xml` file. Any change in the `log-config.xml` file is detected in 2 minutes. So the server need not be restarted when logging levels are changed.

# Notes
As of now, the server does not support TLS.
