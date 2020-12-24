/*
Bus agent

* bcBusPosition: broadcast the current bus position
* bcBusPosition: broadcast the current passengers on the bus
* bcBusPosition: broadcast the current bus speed
* bcBusPosition: broadcast the next stop of the bus
* bcBusPosition: broadcast the number of people waiting at a given stop
* bushold: holds the bus on a stop for a given time
* regulateSpeed: reegulates the speed of the bus
*/

+bcBusPosition(bus,position): bcBusPosition <- .broadcast(tell,busPosition(bus,position)).
+bcBusPassengers(bus,passengers): bcBusPassengers <- .broadcast(tell,passengersOnBus(bus,passengers)).
+bcBusSpeed(bus,speed): bcBusSpeed <- .broadcast(tell,busSpeed(bus,speed)).
+bcBusNextStop(bus,stop): bcBusNextStop <- .broadcast(tell,busNextStop(bus,stop)).
+bcPeopleOnStop(stop,people): bcPeopleOnStop <- .broadcast(tell,peopleOnStop(stop,people)).


+busHold(stop,time): true <- doBushold(stop,time).
+regulateSpeed(bus,speed): true <- doSpeedRegulation(bus,speed).