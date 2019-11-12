// Agent vc in project vacuumcleaner
/*
Very simple vaccum cleaner agent in a world that has only two locations.

Perceptions:
. dirty: the current location has dirty
. clean: the current location is clean
. pos(X): the agent position is X, where X is either l or r.

Actions:
. suck: clean the current location
. left: go to the left positions
. right: go to the right position
*/

// in case I am in a dirty location
+start1: true <- continue1.
+start2: true <- continue2.

// in case I am in a clean location
+clean: pos(l) <- right.
+clean: pos(r) <- left.