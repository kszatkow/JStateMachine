package org.moomin.statemachine;

public class NoBehaviourEventConsumingState extends NoBehaviourState {

	@Override
	public boolean consumesEvent(Event event) {
		return true;
	}
	
}
