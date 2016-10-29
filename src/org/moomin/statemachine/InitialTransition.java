package org.moomin.statemachine;

public class InitialTransition implements StateMachinePart {

	protected State target;
	
	protected TransitionEffect effect;
	
	public InitialTransition(State target, TransitionEffect effect) {
		this.target = target;
		this.effect = effect;
	}

	public State target() {
		return target;
	}
	
	public void takeEffect() {
		effect.execute();
	}
	
	public StateMachine containingStateMachine() {
		// TODO - think if it can stay this way
		return target.containingStateMachine();
	}
}
