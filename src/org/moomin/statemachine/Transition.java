package org.moomin.statemachine;

public class Transition {

	private State fromState;
	
	private State toState;
	
	private Class<? extends Event> eventClass;
	
	private TransitionConstraint guard;
	
	public Transition(State fromState, State toState, 
			Class<? extends Event> eventClass) {
		this(fromState, toState, eventClass, event -> true);
	}

	public Transition(State fromState, State toState, 
			Class<? extends Event> eventClass,
			TransitionConstraint constraint) {
		
		this.fromState = fromState;
		this.toState = toState;
		this.eventClass = eventClass;
		this.guard = constraint;
	}

	public State fromState() {
		return fromState;
	}

	public State toState() {
		return toState;
	}
	
	public Class<? extends Event> triggeredBy() {
		return eventClass;
	}

	public boolean evaluateGuardFor(Event event) {
		return guard.evaluate(event);
	}


}
