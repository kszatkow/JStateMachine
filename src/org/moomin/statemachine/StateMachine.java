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
		transitions.put(transition.getFromState(), transition);
	}

	public State getCurrentState() {
		return currentState;
	}

	public void processEvent(Event event) {
		Transition transition = transitions.get(currentState);
		if (transition != null) {
			if(transition.getEventClass().equals(event.getClass()) 
					&& transition.evaluateContraint(event) ) {
				currentState = transition.getToState();
			}
		}
	}

}
