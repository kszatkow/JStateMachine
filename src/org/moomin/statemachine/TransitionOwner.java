package org.moomin.statemachine;

public interface TransitionOwner {
	
	void addTransition(Transition transition);
	
	void setInitialTransition(PrimitiveTransition initialTransition);
	
}
