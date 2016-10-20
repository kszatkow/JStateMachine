package org.moomin.statemachine;

public class NoBehaviourEventConsumingState extends NoBehaviourState {

	@Override
	public boolean isPassThrough() {
		return false;
	}
	
}
