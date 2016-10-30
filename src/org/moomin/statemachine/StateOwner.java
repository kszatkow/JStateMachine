package org.moomin.statemachine;

public interface StateOwner {
	
	void addState(State state);
	
	void setFinalState(State finalState);

	State activeState();
	
	boolean hasReachedFinalState();
	
}
