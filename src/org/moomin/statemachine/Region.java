package org.moomin.statemachine;

public interface Region extends StateMachinePart, StateOwner, TransitionOwner {

	void deactivate();

	void activate();

	void dispatchInternalEvent(Event event);

	void setInitialTransition(InitialTransition initialTransition);
	
	void setFinalState(State finalState);

	State getActiveState();

	boolean tryConsumingEvent(Event event);
	
	boolean isActive();
	
	boolean hasReachedFinalState();

	// TODO is this method necessary? it is used in tests only
	RegionOwner getOwner();

}