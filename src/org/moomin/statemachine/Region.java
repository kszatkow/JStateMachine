package org.moomin.statemachine;

public interface Region extends StateMachinePart, StateOwner, 
	TransitionOwner, Activatable, EventConsumer {

	// TODO is this method necessary? it is used in tests only
	RegionOwner getOwner();

}