package org.moomin.statemachine;

public interface TransitionGuard {

	public boolean evaluate(Event event);
	
}
