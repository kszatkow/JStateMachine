package org.moomin.statemachine;

public class Transition {

	private State source;
	
	private State target;
	
	private Class<? extends Event> eventClass;
	
	private TransitionConstraint guard;

	private TransitionEffect effect;
	
	public Transition(State source, State target, 
			Class<? extends Event> eventClass) {
		this(source, target, eventClass, event -> true);
	}

	public Transition(State source, State target, 
			Class<? extends Event> eventClass,
			TransitionConstraint guard) {
		this.source = source;
		this.target = target;
		this.eventClass = eventClass;
		this.guard = guard;
		effect = () -> {};
	}

	public Transition(State source, State target, 
			Class<? extends Event> eventClass, TransitionEffect effect) {
		this(source, target, eventClass, event -> true);
		this.effect = effect;
	}

	public State source() {
		return source;
	}

	public State target() {
		return target;
	}
	
	public Class<? extends Event> triggeredBy() {
		return eventClass;
	}

	public boolean evaluateGuardFor(Event event) {
		return guard.evaluate(event);
	}

	public void takeEffect() {
		effect.execute();
	}

}
