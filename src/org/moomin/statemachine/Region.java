package org.moomin.statemachine;

public interface Region extends StateMachinePart {

	void deactivate();

	void activate();

	void dispatchInternalEvent(Event event);

	void setInitialTransition(InitialTransition initialTransition);
	
	void setFinalState(State finalState);

	void addTransition(Transition transition);

	State getActiveState();

	void addState(State substate);
	
	boolean tryConsumingEvent(Event event);
	
	boolean isActive();
	
	boolean hasReachedFinalState();

}