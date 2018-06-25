#!/bin/sh

SERVICE="File Service"
PIDFILE="./fileservice.pid"
JAR_FILE="file-service.jar"
OPTIONS="-Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -Dlog4j.configurationFile=./log-config.xml"

case $1 in
    start)
        echo "Starting $SERVICE server..."
        if [ ! -f $PIDFILE ]; then
            nohup java $OPTIONS -jar $JAR_FILE > /dev/null 2>&1 &
            echo $! > $PIDFILE
            echo "$SERVICE started"
        else
            echo "$SERVICE is running..."
        fi
    ;;

    stop)
        if [ -f $PIDFILE ]; then
            PID=$(cat $PIDFILE);
            echo "Stopping $SERVICE..."
            kill $PID;
            echo "$SERVICE stopped"
            rm $PIDFILE
        else
            echo "$SERVICE is not running ..."
        fi
    ;;

    *)
        echo "Pass argument start/stop"
    ;;
esac
