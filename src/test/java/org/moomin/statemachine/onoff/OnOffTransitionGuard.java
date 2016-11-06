package org.moomin.statemachine.onoff;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.TransitionGuard;
import org.moomin.statemachine.State;

public class OnOffTransitionGuard implements TransitionGuard {

	private boolean evaluateResult = false;
	
	@Override
	public boolean evaluate(State source, Event event) {
		return evaluateResult;
	}
	
	public void evaluateToTrue() {
		evaluateResult = true;
	}

	public void evaluateToFalse() {
		evaluateResult = false;
	}
	
}
