package org.moomin.statemachine;

import java.util.Collection;

public class NoGuardTransition extends Transition {

	public NoGuardTransition(State source, State target, 
			Collection<Class<? extends Event>> triggerableBy,
			TransitionEffect effect) {
		super(source, target, triggerableBy, effect);
	}

	public NoGuardTransition(State source, State target, 
			Collection<Class<? extends Event>> triggerableBy) {
		super(source, target, triggerableBy);
	}
	
	public NoGuardTransition(State source, State target, 
			Class<? extends Event> triggerableBy, TransitionEffect effect) {
		super(source, target, triggerableBy, effect);
	}

	public NoGuardTransition(State source, State target, 
			Class<? extends Event> triggerableBy) {
		super(source, target, triggerableBy);
	}

}
