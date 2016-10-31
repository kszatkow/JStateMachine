package org.moomin.statemachine;

public class PrimitiveTransition implements StateMachinePart {

	protected State target;
	
	protected TransitionEffect effect;
	
	public PrimitiveTransition(State target, TransitionEffect effect) {
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
