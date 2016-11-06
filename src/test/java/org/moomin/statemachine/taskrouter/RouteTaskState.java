package org.moomin.statemachine.taskrouter;

import java.util.List;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.NoBehaviourChoiceState;
import org.moomin.statemachine.Transition;

public class RouteTaskState extends NoBehaviourChoiceState {

	public RouteTaskState(String name) {}

	@Override
	protected Transition selectEnabledTransitionToFire(List<Transition> enabledOutgoingTransitions, Event event) {
		return enabledOutgoingTransitions.get(0);
	}

}
