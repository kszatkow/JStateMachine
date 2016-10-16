package org.moomin.statemachine;

public interface Constraint {

	public boolean evaluate(Event event);
	
}
