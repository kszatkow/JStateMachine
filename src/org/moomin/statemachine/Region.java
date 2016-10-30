package org.moomin.statemachine;

public interface Region extends StateMachinePart, StateOwner, TransitionOwner {

	void deactivate();

	void activate();

	void dispatchInternalEvent(Event event);

	boolean tryConsumingEvent(Event event);
	
	boolean isActive();

	// TODO is this method necessary? it is used in tests only
	RegionOwner getOwner();

}