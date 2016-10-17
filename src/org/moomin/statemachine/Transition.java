package org.moomin.statemachine;

public class Transition {

	private State source;
	
	private State target;
	
	private Class<? extends Event> eventClass;
	
	private TransitionGuard guard;

	private TransitionEffect effect;
	
	public Transition(State source, State target, 
			Class<? extends Event> eventClass, 
			TransitionGuard guard,
			TransitionEffect effect) {
		this.source = source;
		this.target = target;
		this.eventClass = eventClass;
		this.guard = guard;
		this.effect = effect;
	}

	public Transition(State source, State target, 
			Class<? extends Event> eventClass,
			TransitionGuard guard) {
		this(source, target, eventClass, guard, () -> {});
	}

	public Transition(State source, State target, 
			Class<? extends Event> eventClass, TransitionEffect effect) {
		this(source, target, eventClass, event -> true, effect);
	}

	public Transition(State source, State target, 
			Class<? extends Event> eventClass) {
		this(source, target, eventClass, event -> true, () -> {});
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
