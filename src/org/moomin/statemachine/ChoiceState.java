package org.moomin.statemachine;

import java.util.LinkedList;
import java.util.List;

public abstract class ChoiceState extends SimpleState {

	@Override
	public final Transition selectTransitionToFire(List<Transition> outgoingTransitions, 
			Event event) {
		List<Transition> allEnabledOutgoingTransitions = new LinkedList<>();
		for (Transition transition : outgoingTransitions) {
			if( isTransitionEnabled(event, transition) ) {
				allEnabledOutgoingTransitions.add(transition);
			}
		}
		
		if (allEnabledOutgoingTransitions.isEmpty()) {
			throw new IllegalStateException(
					"Ill formed state machine - choice state must have at least one guard evaluating to true");
		}
		
		return selectEnabledTransitionToFire(allEnabledOutgoingTransitions, event);
	}

	protected abstract Transition selectEnabledTransitionToFire(
			List<Transition> enabledOutgoingTransitions, Event event);
	
}
