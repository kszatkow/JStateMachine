package org.moomin.statemachine;

public class ExceptionThrownIllegalTransitionChecker extends ExceptionThrownChecker {
	
	StateMachine stateMachine;
	Transition illegalTransition;
	
	public ExceptionThrownIllegalTransitionChecker(
			Class<? extends Exception> expectedExceptionType,
			String failMessage,
			StateMachine stateMachine) {
		super(expectedExceptionType, failMessage);
		this.stateMachine = stateMachine;
	}

	@Override
	public void doAction() {
		stateMachine.addTransition(illegalTransition);
	}

	public void setIllegalTransition(Transition transition) {
		this.illegalTransition = transition;
	}
}