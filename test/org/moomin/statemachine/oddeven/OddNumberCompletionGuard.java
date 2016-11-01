package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.State;
import org.moomin.statemachine.TransitionGuard;

public final class OddNumberCompletionGuard implements TransitionGuard {
	@Override
	public boolean evaluate(State source, Event event) {
		ParityJunctionState actualState = (ParityJunctionState) source;
		int number = actualState.getLastNumber();
		return ((number == 0) ? false : (number %  2 != 0));
	}
}