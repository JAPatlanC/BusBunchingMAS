/*
Bus agent

Perceptions:
. onRoute: the current location is on route
. onStop: the current location is at a stop
*/

+onRoute: true <- continue1.
+onStop: true <- continue2.

+bcBusPosition(bus,position): bcBusPosition <- .broadcast(tell,busPosition(bus,position)).
+bcPassengers(bus,passengers): bcBusPassengers <- .broadcast(tell,passengersOnBus(bus,passengers)).
+bcBusSpeed(bus,speed): bcBusSpeed <- .broadcast(tell,busSpeed(bus,speed)).
+bcBusNextStop(bus,stop): bcBusNextStop <- .broadcast(tell,busNextStop(bus,stop)).
+bcPeopleOnStop(stop,people): bcPeopleOnStop <- .broadcast(tell,peopleOnStop(stop,people)).


+busHold(stop,time): true <- doBushold(stop,time).
+regulateSpeed(bus,speed): true <- doSpeedRegulation(bus,speed).