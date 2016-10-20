package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.TransitionGuard;

public class ZeroNumberGuard implements TransitionGuard {

	@Override
	public boolean evaluate(Event event) {
		FeedNumberEvent actualEvent = (FeedNumberEvent) event;
		return actualEvent.getNumber() == 0;
	}

}
