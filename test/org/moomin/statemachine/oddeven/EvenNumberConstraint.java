package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.TransitionConstraint;
import org.moomin.statemachine.Event;

public class EvenNumberConstraint implements TransitionConstraint {

	@Override
	public boolean evaluate(Event event) {
		FeedNumberEvent actualEvent = (FeedNumberEvent) event;
		return actualEvent.getNumber() %  2 == 0;
	}

}
