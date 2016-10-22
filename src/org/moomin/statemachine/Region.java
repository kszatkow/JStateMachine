package org.moomin.statemachine;

public interface Region {

	void reset();

	void activate();

	void dispatchEvent(Event event);

	void processEvent();

	void setInitialTransition(InitialTransition initialTransition);

	void addTransition(Transition transition);

	State getActiveState();

	void addState(State substate);

}