package org.moomin.statemachine;

public class InitialTransition {

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
}
