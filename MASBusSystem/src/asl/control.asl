/*
Control agent

Perceptions:
. onRoute: the current location is on route
. onStop: the current location is at a stop
*/

+tellBH(bus,stop,time): tellBH <- .send(bus,tell,busHold(stop,time)).
+validateSkipStop(bus): validateSkipStop <- skipStop(bus).abolish(validateSkipStop(bus)).

+paso1: paso1 <- continue1.
+paso2: paso2 <- continue2.