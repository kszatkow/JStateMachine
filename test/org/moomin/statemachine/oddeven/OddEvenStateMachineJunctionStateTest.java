package org.moomin.statemachine.oddeven;

import org.moomin.statemachine.State;

public abstract class OddEvenStateMachineJunctionStateTest {

	protected State zeroState;
	protected State oddState;
	protected State evenState;
	protected State checkParity;
	
	/** 
	 * Template method.
	 */
	public void test() {
		addStates();
		addJunctionIncomingTransitions();
		addJunctionOutgoingTransitions();
		setInitialTransitionAndActivate();
		assertMachineWorking();
	}
	
	protected abstract void addStates();
	protected abstract void addJunctionIncomingTransitions();
	protected abstract void addJunctionOutgoingTransitions();
	protected abstract void setInitialTransitionAndActivate();
	protected abstract void dispatchProcessEventAndCheckActiveState(
			FeedNumberEvent feedNumberEvent, State expectedActiveState);
	
	private final void assertMachineWorking() {
		// zero -> zero
		dispatchProcessEventAndCheckActiveState(new FeedNumberEvent(0), zeroState);
		// zero -> odd
		dispatchProcessEventAndCheckActiveState(new FeedNumberEvent(3) , oddState);
		// odd -> odd
		dispatchProcessEventAndCheckActiveState(new FeedNumberEvent(5) , oddState);
		// odd -> zero
		dispatchProcessEventAndCheckActiveState(new FeedNumberEvent(0) , zeroState);
		// zero -> even
		dispatchProcessEventAndCheckActiveState(new FeedNumberEvent(2) , evenState);
		// even -> even
		dispatchProcessEventAndCheckActiveState(new FeedNumberEvent(12) , evenState);
		// even -> zero
		dispatchProcessEventAndCheckActiveState(new FeedNumberEvent(0) , zeroState);
		// zero -> odd
		dispatchProcessEventAndCheckActiveState(new FeedNumberEvent(13) , oddState);
		// odd -> even
		dispatchProcessEventAndCheckActiveState(new FeedNumberEvent(18) , evenState);
		// even -> odd
		dispatchProcessEventAndCheckActiveState(new FeedNumberEvent(15) , oddState);
	}

}
