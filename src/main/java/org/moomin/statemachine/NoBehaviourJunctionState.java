package org.moomin.statemachine;

public class NoBehaviourJunctionState extends JunctionState {

	protected NoBehaviourJunctionState(String string) {
		super(string);
	}

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
