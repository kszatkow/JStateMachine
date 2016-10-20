package org.moomin.statemachine;

public class InitialTransitionTestEffect implements TransitionEffect {

	private boolean executed = false;
	
	@Override
	public void execute() {
		executed = true;
	}

	public boolean hasBeenExecuted() {
		return executed;
	}
}
