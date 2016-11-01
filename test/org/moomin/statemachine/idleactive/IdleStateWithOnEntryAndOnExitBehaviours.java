package org.moomin.statemachine.idleactive;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.SimpleState;

public class IdleStateWithOnEntryAndOnExitBehaviours extends SimpleState {

	private boolean onEntryDone = false;
	
	private boolean onExitDone = false;
	
	public IdleStateWithOnEntryAndOnExitBehaviours(String name) {}
	
	@Override
	public void onEntryBehaviour(Event event) {
		onEntryDone = true;
	}

	@Override
	public void doActionBehaviour() {
		// empty on purpose
	}

	@Override
	public void onExitBehaviour() {
		onExitDone = true;
	}

	public boolean hasOnEntryBeenExecuted() {
		return onEntryDone;
	}
	
	public boolean hasOnExitBeenExecuted() {
		return onExitDone;
	}
	
	public void clearExecutionStateFlags() {
		onEntryDone = false;
		onExitDone = false;
	}

}
