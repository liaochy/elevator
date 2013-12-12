#!/bin/sh

export LANG=en_US.UTF-8

bin=`dirname "$0"`
ELEVATOR_HOME=`cd "$bin/.."; pwd -P`
ELEVATOR_CONF_DIR=$ELEVATOR_HOME/config

ELEVATOR_LOGS=/opt/data/goldmine/elevator/logs
mkdir -p $ELEVATOR_LOGS

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

logname=log-`date +%Y%m%d%H%M%S`
touch $ELEVATOR_LOGS/$logname
echo =================================starting the bee keeper at `date` ========================================
JAVA_OPT="-server -Xms3072m -Xmx3072m -XX:NewSize=256m -XX:MaxNewSize=256m -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -Xloggc:$ELEVATOR_LOGS/gc.log -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$ELEVATOR_LOGS/gc_dump"
nohup java $JAVA_OPT -Djava.library.path=$JAVA_LIBRARY_PATH -Delevator.home=$ELEVATOR_HOME -classpath $CLASSPATH:$ELEVATOR_HOME/elevator.jar com.sohu.tw.elevator.server.ServerBootstrap $1>>$ELEVATOR_LOGS/$logname 2>&1 &

# add pid file
PID_FOLDER=$ELEVATOR_HOME/pids
mkdir -p $PID_FOLDER
echo $!>$PID_FOLDER/elevator.pid
echo "pid file created successfully"
