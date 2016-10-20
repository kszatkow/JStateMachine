package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.TransitionGuard;
import org.moomin.statemachine.Event;

public class EvenNumberGuard implements TransitionGuard {

	@Override
	public boolean evaluate(Event event) {
		FeedNumberEvent actualEvent = (FeedNumberEvent) event;
		int number = actualEvent.getNumber();
		return ((number == 0) ? false : (number %  2 == 0));
	}

}
