package org.moomin.statemachine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StateMachine {

	private State activeState;
	
	private Set<State> states = new HashSet<State>();
	
	private Map<State, List<Transition>> transitions = new HashMap<>();
	
	
	public void addState(State state) {
		if (states.contains(state)) {
			throw new IllegalArgumentException("Duplicate state.");
		}
		
		states.add(state);
		initializeTransitionsFromState(state);
	}

	private void initializeTransitionsFromState(State state) {
		List<Transition> transitionsFromSource = new LinkedList<>();
		transitions.put(state, transitionsFromSource);
	}

	public void setInitialState(State initialState) {
		if (!states.contains(initialState)) {
			throw new IllegalArgumentException("Invalid initial state - state not contained in the state machine.");
		}
		
		activeState = initialState;
		activeState.onEntry();
	}

	public void addTransition(Transition transition) {
		if (!states.contains(transition.source()) || !states.contains(transition.target()) ) {
			throw new IllegalArgumentException("Invalid source of destination state.");
		}
		
		List<Transition> transitionsFromSource = transitions.get(transition.source());
		transitionsFromSource.add(transition);
	}

	public State getActiveState() {
		return activeState;
	}

	public void processEvent(Event event) {
		List<Transition> outgoingFromActiveState = transitions.get(activeState);
		for (Transition transition : outgoingFromActiveState) {
			if( isTransitionEnabled(event, transition) ) {
				activeState.onExit();
				transition.takeEffect();
				activeState = transition.target();
				activeState.onEntry();
				activeState.doAction();
				break ;
			}
		}
	}

	private boolean isTransitionEnabled(Event event, Transition transition) {
		return transition.isTriggerableBy(event) 
				&& transition.evaluateGuardFor(event);
	}

}
