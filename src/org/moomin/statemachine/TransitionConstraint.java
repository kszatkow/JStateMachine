package org.moomin.statemachine;

public interface TransitionConstraint {

	public boolean evaluate(Event event);
	
}
