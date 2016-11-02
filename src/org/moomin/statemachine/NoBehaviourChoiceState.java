package org.moomin.statemachine;

import java.util.List;

public abstract class NoBehaviourChoiceState extends ChoiceState {

	@Override
	protected abstract Transition selectEnabledTransitionToFire(
			List<Transition> enabledOutgoingTransitions, Event event);
		
	@Override
	protected final void onEntryBehaviour(Event entryEvent) {
		// empty on purpose
	}

	@Override
	protected final void doActionBehaviour() {
		// empty on purpose
	}

	@Override
	protected final void onExitBehaviour() {
		// empty on purpose
	}

}
