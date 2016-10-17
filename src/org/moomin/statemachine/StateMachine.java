package org.moomin.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StateMachine {

	private State initialState;

	private State currentState;
	
	private List<State> states = new ArrayList<State>();
	
	private Map<State, List<Transition>> transitions = new HashMap<>();
	
	
	public void addState(State state) {
		states.add(state);
	}

	public void setInitialState(State state) {
		initialState = state;
		currentState = initialState;
	}

	public void addTransition(Transition transition) {
		if (transitions.containsKey(transition.fromState())) {
			List<Transition> transitionsFromState = transitions.get(transition.fromState());
			transitionsFromState.add(transition);
		} else {
			List<Transition> transitionsFromState = new LinkedList<>();
			transitionsFromState.add(transition);
			transitions.put(transition.fromState(), transitionsFromState);
		}
	}

	public State getCurrentState() {
		return currentState;
	}

	public void processEvent(Event event) {
		List<Transition> transitionsFromCurrentState = transitions.get(currentState);
		if (!transitionsFromCurrentState.isEmpty()) {
			for (Transition transition : transitionsFromCurrentState) {
				if(isTransitionPossible(event, transition) ) {
					// TODO what if toState is not in the state machine?
					currentState = transition.toState();
					break ;
				}
			}
		}
	}

	private boolean isTransitionPossible(Event event, Transition transition) {
		return transition.triggeredBy().isInstance(event) 
				&& transition.evaluateGuardFor(event);
	}

}
