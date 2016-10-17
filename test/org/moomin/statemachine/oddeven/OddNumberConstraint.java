package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.TransitionGuard;
import org.moomin.statemachine.Event;

public class OddNumberConstraint implements TransitionGuard {

	@Override
	public boolean evaluate(Event event) {
		FeedNumberEvent actualEvent = (FeedNumberEvent) event;
		return actualEvent.getNumber() %  2 != 0;
	}

}
