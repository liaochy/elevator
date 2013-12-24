#!/bin/sh

export LANG=en_US.UTF-8

rotate_log ()
{
    log=$1;
    num=5;
    if [ -n "$2" ]; then
        num=$2
    fi
    if [ -f "$log" ]; then # rotate logs
        while [ $num -gt 1 ]; do
            prev=`expr $num - 1`
            [ -f "$log.$prev" ] && mv "$log.$prev" "$log.$num"
            num=$prev
        done
        mv "$log" "$log.$num";
    fi
}

bin=`dirname "$0"`
ELEVATOR_HOME=`cd "$bin/.."; pwd -P`
ELEVATOR_CONF_DIR=$ELEVATOR_HOME/config

ELEVATOR_LOGS=/opt/data/goldmine/elevator/logs
mkdir -p $ELEVATOR_LOGS

PID_FOLDER=$ELEVATOR_HOME/pids
mkdir -p $PID_FOLDER

# add all lib and config in classpath 
ELE_LIB=$ELEVATOR_HOME/lib
CLASSPATH=$ELEVATOR_CONF_DIR
for jar in `ls $ELE_LIB/*.jar`
do
      CLASSPATH="$CLASSPATH:""$jar"
done

if [ -d "$ELEVATOR_HOME/elevator-webapps" ]; then
    CLASSPATH=${CLASSPATH}:$ELEVATOR_HOME
fi
CLASSPATH=$CLASSPATH:$ELEVATOR_HOME/config:$JAVA_HOME/lib/tools.jar

JOPTS="-Delevator.log.dir=${ELEVATOR_LOGS:-${ELEVATOR_HOME}/logs} "
JOPTS="$JOPTS -Delevator.log.file=${ELEVATOR_LOGFILE:-elevator.log} "
JOPTS="$JOPTS -Delevator.root.logger=${ELEVATOR_ROOT_LOGGER:-INFO,DRFA} "
JOPTS="$JOPTS -Dkafka.root.logger=${KAFKA_ROOT_LOGGER:-INFO,kafka} "
JOPTS="$JOPTS -Dzookeeper.root.logger=${ZOOKEEPER_ROOT_LOGGER:-ERROR,zookeeper} "
JOPTS="$JOPTS -server -Xms3072m -Xmx3072m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -Xloggc:$ELEVATOR_LOGS/gc.log -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$ELEVATOR_LOGS/gc_dump"

log=$ELEVATOR_LOGS/elevator.out
rotate_log $log
nohup java $JOPTS -Djava.library.path=$JAVA_LIBRARY_PATH -Delevator.home=$ELEVATOR_HOME -classpath $CLASSPATH:$ELEVATOR_HOME/elevator.jar com.sohu.tw.elevator.server.ServerBootstrap > "$log" 2>&1 < /dev/null &

# add pid file
echo $!>$PID_FOLDER/elevator.pid
echo "pid file created successfully"
