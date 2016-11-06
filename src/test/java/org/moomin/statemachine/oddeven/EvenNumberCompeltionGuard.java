package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.State;
import org.moomin.statemachine.TransitionGuard;

public final class EvenNumberCompeltionGuard implements TransitionGuard {
	@Override
	public boolean evaluate(State source, Event event) {
		CheckParityJunctionState actualState = (CheckParityJunctionState) source;
		int number = actualState.getLastNumber();
		return ((number == 0) ? false : (number %  2 == 0));
	}
}