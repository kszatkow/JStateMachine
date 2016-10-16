package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.Constraint;
import org.moomin.statemachine.Event;

public class EvenNumberConstraint implements Constraint {

	@Override
	public boolean evaluate(Event event) {
		FeedNumberEvent actualEvent = (FeedNumberEvent) event;
		return actualEvent.getNumber() %  2 == 0;
	}

}
