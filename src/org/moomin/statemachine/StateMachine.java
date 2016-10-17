package org.moomin.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StateMachine {

	private State activeState;
	
	private List<State> states = new ArrayList<State>();
	
	private Map<State, List<Transition>> transitions = new HashMap<>();
	
	
	public void addState(State state) {
		states.add(state);
	}

	public void setInitialState(State initialState) {
		activeState = initialState;
	}

	public void addTransition(Transition transition) {
		State sourceState = transition.source();
		if (transitions.containsKey(sourceState)) {
			List<Transition> transitionsFromState = transitions.get(sourceState);
			transitionsFromState.add(transition);
		} else {
			List<Transition> transitionsFromState = new LinkedList<>();
			transitionsFromState.add(transition);
			transitions.put(sourceState, transitionsFromState);
		}
	}

	public State getActiveState() {
		return activeState;
	}

	public void processEvent(Event event) {
		List<Transition> outgoingFromActiveState = transitions.get(activeState);
		for (Transition transition : outgoingFromActiveState) {
			// TODO what is multiple transitions are enabled?
			if( isTransitionEnabled(event, transition) ) {
				// TODO what if toState is not in the state machine? - this should not happen - such transitions should not be accepted
				activeState = transition.target();
				break ;
			}
		}
	}

	private boolean isTransitionEnabled(Event event, Transition transition) {
		return transition.triggeredBy().isInstance(event) 
				&& transition.evaluateGuardFor(event);
	}

}
