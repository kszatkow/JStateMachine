package org.moomin.statemachine.idleactive;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.State;

public class IdleStateWithOnEntryAndOnExitBehaviours implements State {

	private boolean onEntryDone = false;
	
	private boolean onExitDone = false;
	
	public IdleStateWithOnEntryAndOnExitBehaviours(String name) {}
	
	@Override
	public void onEntry() {
		onEntryDone = true;
	}

	@Override
	public void doAction() {
		// empty on purpose
	}

	@Override
	public void onExit() {
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

	@Override
	public boolean consumesEvent(Event event) {
		return true;
	}
}
