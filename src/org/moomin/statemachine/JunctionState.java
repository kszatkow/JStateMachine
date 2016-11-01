package org.moomin.statemachine;

import java.util.LinkedList;
import java.util.List;

public abstract class JunctionState extends SimpleState {

	private Transition elseTransition = null;
	
	public JunctionState(String string) {}

	public void addElseTrasition(Transition elseTransition) {
		this.elseTransition = elseTransition;
	}
	
	@Override
	public Transition selectTransitionToFire(List<Transition> outgoingTransitions, 
			Event event) {
		List<Transition> allOutgoingTransitions = new LinkedList<>(outgoingTransitions);
		if (elseTransition != null) {
			allOutgoingTransitions.add(elseTransition);
		}
		
		return super.selectTransitionToFire(allOutgoingTransitions, event);
	}
	
}
