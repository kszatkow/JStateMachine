package org.moomin.statemachine.idleactive;

import org.moomin.statemachine.SimpleState;

public class ActiveStateWithDoActionBehaviour extends SimpleState {

	private boolean onActionDone = false;
	
	public ActiveStateWithDoActionBehaviour(String name) {}
	
	@Override
	public void onEntryBehaviour() {
		// empty on purpose

	}

	@Override
	public void doActionBehaviour() {
		onActionDone = true;
	}

	@Override
	public void onExitBehaviour() {
		// empty on purpose
	}

	public boolean hasOnActionBeenExecuted() {
		return onActionDone;
	}

	@Override
	public boolean isPassThrough() {
		return true;
	}

}
