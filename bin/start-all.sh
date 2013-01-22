ELEVATOR_HOME=`pwd|xargs dirname`
ELEVATOR_HOME_A=`cat "$ELEVATOR_HOME"/config/elevatorserver`
echo $saELEVATOR_HOME_A;
for elevatorserver in `cat "$ELEVATOR_HOME"/config/elevatorserver |xargs`; do
	echo $elevatorserver
	 ssh $elevatorserver $ELEVATOR_HOME/bin/start.sh $ELEVATOR_HOME  | sed "s/^/$elevatorserver: /"
done
