package org.moomin.statemachine.oddeven;

public abstract class OddEvenStateMachineJunctionStateTestTemplateMethod {

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
	protected abstract void assertMachineWorking();
	
}
