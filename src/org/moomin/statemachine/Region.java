package org.moomin.statemachine;

public interface Region {

	void deactivate();

	void activate();

	void dispatchInternalEvent(Event event);

	void setInitialTransition(InitialTransition initialTransition);

	void addTransition(Transition transition);

	State getActiveState();

	void addState(State substate);
	
	boolean tryConsumingEvent(Event event);
	
	boolean isActive();

}