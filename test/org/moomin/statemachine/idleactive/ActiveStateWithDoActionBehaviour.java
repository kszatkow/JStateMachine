package org.moomin.statemachine.idleactive;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.State;

public class ActiveStateWithDoActionBehaviour implements State {

	private boolean onActionDone = false;
	
	public ActiveStateWithDoActionBehaviour(String name) {}
	
	@Override
	public void onEntry() {
		// empty on purpose

	}

	@Override
	public void doAction() {
		onActionDone = true;
	}

	@Override
	public void onExit() {
		// empty on purpose
	}

	public boolean hasOnActionBeenExecuted() {
		return onActionDone;
	}

	@Override
	public boolean consumesEvent(Event event) {
		return true;
	}

}
