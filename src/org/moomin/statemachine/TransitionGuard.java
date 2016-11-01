package org.moomin.statemachine;

public interface TransitionGuard {

	public boolean evaluate(State source, Event event);
	
}
