package org.moomin.statemachine;

public class JunctionState extends NoBehaviourSimpleState {

	private Transition elseTransition = null;
	
	public JunctionState(String string) {}

	public void addElseTrasition(Transition elseTransition) {
		this.elseTransition = elseTransition;
	}
	
	public Transition getElseTransition() {
		return elseTransition;
	}

}
