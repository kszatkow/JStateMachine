package org.moomin.statemachine;

public class CompletionTransition extends Transition {

	public CompletionTransition(State source, State target, 
			TransitionGuard guard, TransitionEffect effect) {
		super(source, target, CompletionEvent.class, guard, effect);
	}
	
	public CompletionTransition(State source, State target, TransitionGuard guard) {
		super(source, target, CompletionEvent.class, guard);
	}
	
	public CompletionTransition(State source, State target, TransitionEffect effect) {
		super(source, target, CompletionEvent.class, effect);
	}
	
	public CompletionTransition(State source, State target) {
		super(source, target, CompletionEvent.class);
	}
}
