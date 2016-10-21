package org.moomin.statemachine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PrimitiveStateMachine {

	private boolean isActive = false;
	private InitialTransition initialTransition;
	private Set<State> states = new HashSet<State>();
	private Map<State, List<Transition>> transitions = new HashMap<>();
	private State activeState = State.NULL_STATE;

	public PrimitiveStateMachine() {
		super();
	}

	public void addState(State state) {
		throwIfInIllegalActiveState("State addition is not allowed when state machine is active.");
		
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

	public void addTransition(Transition transition) {
		throwIfInIllegalActiveState("Transition addition is not allowed when state machine is active.");
		
		if (!states.contains(transition.source()) || !states.contains(transition.target()) ) {
			throw new IllegalArgumentException("Invalid source of destination state.");
		}
		
		List<Transition> transitionsFromSource = transitions.get(transition.source());
		transitionsFromSource.add(transition);
	}

	public void setInitialTransition(InitialTransition initialTransition) {
		throwIfInIllegalActiveState("Initial transition setup is not allowed when state machine is active.");
		
		if (!states.contains(initialTransition.target())) {
			throw new IllegalArgumentException("Invalid default state - state not contained in the state machine.");
		}
		
		this.initialTransition = initialTransition; 
	}

	public State getActiveState() {
		throwIfInIllegalInactiveState("State machine is inactive - no state is active at this stage, activate first.");
		
		return activeState;
	}

	public void processEvent(Event event) {
		throwIfInIllegalInactiveState("Even processing not allowed when state machine is inactive, activate first.");
		
		if (activeState.isComposite()) {
			SimpleCompositeState activeCompositeState = (SimpleCompositeState) activeState;
			activeCompositeState.processEvent(event);
		}
		
		//TODO - how to process transition without a trigger?
		List<Transition> outgoingFromActiveState = transitions.get(activeState);
		for (Transition transition : outgoingFromActiveState) {
			if( isTransitionEnabled(event, transition) ) {
				fireTransition(transition);
				if (activeState.isPassThrough()) {
					processEvent(event);
				}
				break ;
			}
		}
	}

	private boolean isTransitionEnabled(Event event, Transition transition) {
		return transition.isTriggerableBy(event) 
				&& transition.evaluateGuardFor(event);
	}

	private void fireTransition(InitialTransition transition) {
		activeState.onExit();
		if (activeState.isComposite()) {
			SimpleCompositeState activeCompositeState = (SimpleCompositeState) activeState;
			activeCompositeState.reset();
		}
		transition.takeEffect();
		activeState = transition.target();
		activeState.onEntry();
		activeState.doAction();
		if (activeState.isComposite()) {
			SimpleCompositeState activeCompositeState = (SimpleCompositeState) activeState;
			activeCompositeState.activate();
		}
	}

	public void activate() {
		throwIfInIllegalActiveState("State machine is already active.");
		
		isActive = true;
		fireTransition(initialTransition);
	}
	
	private void throwIfInIllegalActiveState(String exceptionMessage) {
		if (isActive) {
			throw new IllegalStateException(exceptionMessage);
		}
	}
	
	private void throwIfInIllegalInactiveState(String exceptionMessage) {
		if (!isActive) {
			throw new IllegalStateException(exceptionMessage);
		}
	}
	
	protected void deactivate() {
		isActive = false;
		activeState = State.NULL_STATE;
	}
}