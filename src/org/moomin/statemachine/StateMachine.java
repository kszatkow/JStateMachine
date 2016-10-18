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
		initializeTransitionsFromState(state);
	}

	private void initializeTransitionsFromState(State state) {
		List<Transition> transitionsFromSource = new LinkedList<>();
		transitions.put(state, transitionsFromSource);
	}

	public void setInitialState(State initialState) {
		activeState = initialState;
	}

	public void addTransition(Transition transition) {
		State sourceState = transition.source();
		List<Transition> transitionsFromSource = transitions.get(sourceState);
		transitionsFromSource.add(transition);
	}

	public State getActiveState() {
		return activeState;
	}

	public void processEvent(Event event) {
		List<Transition> outgoingFromActiveState = transitions.get(activeState);
		for (Transition transition : outgoingFromActiveState) {
			if( isTransitionEnabled(event, transition) ) {
				// TODO what if toState is not in the state machine? - this should not happen - such transitions should not be accepted
				transition.takeEffect();
				activeState = transition.target();
				break ;
			}
		}
	}

	private boolean isTransitionEnabled(Event event, Transition transition) {
		return transition.isTriggerableBy(event) 
				&& transition.evaluateGuardFor(event);
	}

}
