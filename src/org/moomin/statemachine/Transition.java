package org.moomin.statemachine;

public class Transition {

	private State fromState;
	
	private State toState;
	
	private Class<?> eventClass;
	
	private Constraint constraint;
	
	public Transition(State fromState, State toState, 
			Class<? extends Event> eventClass) {
		this.fromState = fromState;
		this.toState = toState;
		this.eventClass = eventClass;
		this.constraint = new Constraint() {
			@Override
			public boolean evaluate(Event event) {
				return true;
			}
		};
	}

	public Transition(State fromState, State toState, 
			Class<? extends Event> eventClass,
			Constraint constraint) {
		this(fromState, toState, eventClass);
		this.constraint = constraint;
	}

	public State getFromState() {
		return fromState;
	}

	public State getToState() {
		return toState;
	}
	
	public Class<?> getEventClass() {
		return eventClass;
	}

	public boolean evaluateContraint(Event event) {
		return constraint.evaluate(event);
	}


}
