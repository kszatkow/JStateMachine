package org.moomin.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateMachine {

	private State initialState;

	private State currentState;
	
	private List<State> states = new ArrayList<State>();
	
	private Map<State, Transition> transitions = new HashMap<>();
	
	
	public void addState(State state) {
		states.add(state);
	}

	public void setInitialState(State state) {
		initialState = state;
		currentState = initialState;
	}

	public void addTransition(Transition transition) {
		transitions.put(transition.fromState(), transition);
	}

	public State getCurrentState() {
		return currentState;
	}

	public void processEvent(Event event) {
		Transition transition = transitions.get(currentState);
		// TODO handle multiple transitions from one state
		if (transition != null) {
			if(isTransitionPossible(event, transition) ) {
				// TODO what if toState is not in the state machine?
				currentState = transition.toState();
			}
		}
	}

	private boolean isTransitionPossible(Event event, Transition transition) {
		return transition.triggeredBy().isInstance(event) 
				&& transition.evaluateGuardFor(event);
	}

}
