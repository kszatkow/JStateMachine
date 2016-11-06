package org.moomin.statemachine;

public class ExceptionThrownIllegalTransitionChecker extends ExceptionThrownChecker {
	
	Region stateMachineRegion;
	Transition illegalTransition;
	
	public ExceptionThrownIllegalTransitionChecker(
			Class<? extends Exception> expectedExceptionType,
			String failMessage,
			Region stateMachineRegion) {
		super(expectedExceptionType, failMessage);
		this.stateMachineRegion = stateMachineRegion;
	}

	@Override
	public void doAction() {
		stateMachineRegion.addTransition(illegalTransition);
	}

	public void setIllegalTransition(Transition transition) {
		this.illegalTransition = transition;
	}
}