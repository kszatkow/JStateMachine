package org.moomin.statemachine.onoff;

import org.moomin.statemachine.Event;
import org.moomin.statemachine.TransitionGuard;

public class OnOffTransitionGuard implements TransitionGuard {

	private boolean evaluateResult = false;
	
	@Override
	public boolean evaluate(Event event) {
		return evaluateResult;
	}
	
	public void evaluateToTrue() {
		evaluateResult = true;
	}

	public void evaluateToFalse() {
		evaluateResult = false;
	}
	
}
