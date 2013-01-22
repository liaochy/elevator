#!/bin/sh

bin=`dirname "$0"`
ELEVATOR_HOME=`cd "$bin/.."; pwd -P`
if [ "$1" != "" ]; then
	ELEVATOR_HOME=$1
fi
PID_FILE=$ELEVATOR_HOME/pids/elevator.pid

PID=`cat $PID_FILE`
if [ -f "$PID_FILE" ]; then 
	`cat "$PID_FILE" | xargs kill -9`
	echo "kill the old server successfully"

fi 



