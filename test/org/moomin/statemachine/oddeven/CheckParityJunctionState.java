package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.JunctionState;

public class CheckParityJunctionState extends JunctionState {

	private int lastNumber = 0; 
	
	public CheckParityJunctionState(String string) {
		super(string);
	}
	
	@Override
	protected void onEntryBehaviour(Event entryEvent) {
		FeedNumberEvent actualEvent = (FeedNumberEvent) entryEvent;
		lastNumber = actualEvent.getNumber();
	}

	@Override
	protected void doActionBehaviour() {
		// empty on purpose
	}

	@Override
	protected void onExitBehaviour() {
		// empty on purpose
	}
	
	public int getLastNumber() {
		return lastNumber;
	}
	
}
