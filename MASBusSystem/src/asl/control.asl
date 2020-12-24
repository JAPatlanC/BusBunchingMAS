/*
Control agent

. tellBH: tells a bus to do bus holding
. validateSkipStop: checks if the bus must skip the next stop
* start: keeps the simulation going
* finish: ends the simulation
*/
!start.

+!tellBH(BUS,STOP,TIME): tellBH <- .send(BUS,tell,busHold(STOP,TIME)).

+!validateSkipStop(BUS): validateSkipStop(BUS) <- skipStop(BUS).abolish(validateSkipStop(BUS))!start.

+!start: start & tellBH(BUS,STOP,TIME) & not validateSkipStop(BUS) & not finish <- !tellBH(BUS,STOP,TIME).
+!start: start & validateSkipStop(BUS) & not tellBH(BUS,STOP,TIME) & not finish <- !validateSkipStop(BUS).
+!start: start & not validateSkipStop(BUS) & not tellBH(BUS,STOP,TIME) & not finish<- start;!start.
+!start:finish<- .print("Simulation end").