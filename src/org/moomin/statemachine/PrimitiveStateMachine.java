package org.moomin.statemachine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PrimitiveStateMachine implements Region {

	private StateMachine owningStateMachine;
	private boolean isActive = false;
	private InitialTransition initialTransition;
	private Set<State> states = new HashSet<State>();
	private Map<State, List<Transition>> transitions = new HashMap<>();
	private State activeState = State.NULL_STATE;
	private State finalState;

	public PrimitiveStateMachine(StateMachine owningStateMachine) {
		this.owningStateMachine = owningStateMachine;
	}

	@Override
	public void addState(State state) {
		throwIfInIllegalActiveState("State addition is not allowed when state machine is active.");
		
		if (states.contains(state)) {
			throw new IllegalArgumentException("Duplicate state.");
		}
		
		states.add(state);
		initializeTransitionsFromState(state);
		state.assignOwner(this);
	}

	private void initializeTransitionsFromState(State state) {
		List<Transition> transitionsFromSource = new LinkedList<>();
		transitions.put(state, transitionsFromSource);
	}

	@Override
	public void addTransition(Transition transition) {
		throwIfInIllegalActiveState("Transition addition is not allowed when state machine is active.");
		
		if (!states.contains(transition.source()) || !states.contains(transition.target()) ) {
			throw new IllegalArgumentException("Invalid source of destination state.");
		}
		
		List<Transition> transitionsFromSource = transitions.get(transition.source());
		transitionsFromSource.add(transition);
	}

	@Override
	public void setInitialTransition(InitialTransition initialTransition) {
		throwIfInIllegalActiveState("Initial transition setup is not allowed when state machine is active.");
		
		if (!states.contains(initialTransition.target())) {
			throw new IllegalArgumentException("Invalid default state - state not contained in the state machine.");
		}
		
		this.initialTransition = initialTransition; 
	}

	@Override
	public State getActiveState() {
		throwIfInIllegalInactiveState("State machine is inactive - no state is active at this stage, activate first.");
		
		return activeState;
	}

	@Override
	public void dispatchInternalEvent(Event event) {
		owningStateMachine.dispatchEventToQueueFront(event);
	}

	private boolean isTransitionEnabled(Event event, Transition transition) {
		return transition.isTriggerableBy(event) 
				&& transition.evaluateGuardFor(event);
	}

	// TODO - why is this initial transition?
	private void fireTransition(InitialTransition transition) {
		activeState.onExit();
		transition.takeEffect();
		activeState = transition.target();
		activeState.onEntry();
		activeState.doAction();
	}

	@Override
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
	
	@Override
	public void deactivate() {
		isActive = false;
		activeState = State.NULL_STATE;
	}

	@Override
	public boolean tryConsumingEvent(Event event) {
		if (activeState.tryConsumingEvent(event)) {
			return true;
		}
		
		List<Transition> outgoingFromActiveState = transitions.get(activeState);
		for (Transition transition : outgoingFromActiveState) {
			if( isTransitionEnabled(event, transition) ) {
				fireTransition(transition);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public void setFinalState(State finalState) {
		this.finalState = finalState;
		
	}

	@Override
	public boolean hasReachedFinalState() {
		return activeState == finalState;
	}

	@Override
	public StateMachine containingStateMachine() {
		return owningStateMachine;
	}

}