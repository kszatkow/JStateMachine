package org.moomin.statemachine.taskrouter;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.SimpleState;

public class NodeState extends SimpleState {

	private boolean isBusy = false;

	public NodeState(String name) {}

	public void setIdle() {
		isBusy = false;
	}
	
	public boolean isBusy() {
		return isBusy;
	}

	@Override
	protected void onEntryBehaviour(Event entryEvent) {
		isBusy = true;
	}

	@Override
	protected void doActionBehaviour() {
		// empty on purpose
	}

	@Override
	protected void onExitBehaviour() {
		// empty on purpose
	}

}
