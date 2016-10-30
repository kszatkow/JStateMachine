package org.moomin.statemachine;

public interface Region extends StateMachinePart, StateOwner, 
	TransitionOwner, Activatable {

	boolean tryConsumingEvent(Event event);
	
	// TODO is this method necessary? it is used in tests only
	RegionOwner getOwner();

}