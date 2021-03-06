package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.TransitionGuard;
import org.moomin.statemachine.Event;
import org.moomin.statemachine.State;

public class EvenNumberGuard implements TransitionGuard {

	@Override
	public boolean evaluate(State source, Event event) {
		FeedNumberEvent actualEvent = (FeedNumberEvent) event;
		int number = actualEvent.getNumber();
		return ((number == 0) ? false : (number %  2 == 0));
	}

}
