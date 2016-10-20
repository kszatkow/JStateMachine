package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.NoBehaviourState;

public class CheckParity extends NoBehaviourState {

	public CheckParity(String string) { }

	@Override
	public boolean consumesEvent(Event event) {
		return false;
	}

}
