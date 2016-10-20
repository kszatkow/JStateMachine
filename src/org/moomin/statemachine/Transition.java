package org.moomin.statemachine;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Transition extends InitialTransition {

	private State source;
	
	private Set<Class<? extends Event>> triggerableBy = new HashSet<>();
	
	private TransitionGuard guard;

	
	public Transition(State source, State target, 
			Collection<Class<? extends Event>> triggerableBy,
			TransitionGuard guard,
			TransitionEffect effect) {
		super(target, effect);
		this.source = source;
		this.guard = guard; 
		this.triggerableBy.addAll(triggerableBy);
	}
	
	public Transition(State source, State target, 
			Collection<Class<? extends Event>> triggerableBy,
			TransitionGuard guard) {
		this(source, target, triggerableBy, guard, () -> {});
	}

	public Transition(State source, State target, 
			Collection<Class<? extends Event>> triggerableBy, 
			TransitionEffect effect) {
		this(source, target, triggerableBy, event -> true, effect);
	}

	public Transition(State source, State target, 
			Collection<Class<? extends Event>> triggerableBy) {
		this(source, target, triggerableBy, event -> true, () -> {});
	}
	
	public Transition(State source, State target, 
			Class<? extends Event> eventClass, 
			TransitionGuard guard,
			TransitionEffect effect) {
		this(source, target, Collections.singletonList(eventClass), guard, effect);
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

	public boolean isTriggerableBy(Event event) {
		for (Class<? extends Event> eventClass : triggerableBy) {
			if (eventClass.isInstance(event)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean evaluateGuardFor(Event event) {
		return guard.evaluate(event);
	}

}
