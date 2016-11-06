package org.moomin.statemachine;

public class CompletionEvent implements Event {

	private State source;

	public CompletionEvent(State source) {
		this.source = source;
	}
	
	public State getSource() {
		return source;
	}
}
