package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.State;
import org.moomin.statemachine.TransitionGuard;

public class ZeroNumberGuard implements TransitionGuard {

	@Override
	public boolean evaluate(State source, Event event) {
		FeedNumberEvent actualEvent = (FeedNumberEvent) event;
		return actualEvent.getNumber() == 0;
	}

}
