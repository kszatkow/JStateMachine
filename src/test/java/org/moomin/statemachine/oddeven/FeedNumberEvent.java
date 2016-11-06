package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.Event;

public class FeedNumberEvent implements Event {

	private int number;
	
	public FeedNumberEvent(int num) {
		number = num;
	}

	public int getNumber() {
		return number;
	}

}
