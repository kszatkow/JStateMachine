package org.moomin.statemachine.taskrouter;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.State;
import org.moomin.statemachine.TransitionGuard;

public class NodeBusyGuard implements TransitionGuard {

	private NodeState nodeState;

	public NodeBusyGuard(NodeState nodeState) {
		this.nodeState = nodeState;
	}

	@Override
	public boolean evaluate(State source, Event event) {
		return !nodeState.isBusy();
	}

}
