package org.moomin.statemachine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegionStateMachine extends SinglyActivatableObject implements Region {

	private RegionOwner owner;
	private PrimitiveTransition initialTransition;
	private Set<State> states = new HashSet<State>();
	private Map<State, List<Transition>> transitions = new HashMap<>();
	private State activeState = State.NULL_STATE;
	private State finalState;

	public RegionStateMachine(RegionOwner owner) {
		this.owner = owner;
	}

	@Override
	public void addState(State state) {
		assertInactive("State addition is not allowed when state machine is active.");
		
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
		assertInactive("Transition addition is not allowed when state machine is active.");
		
		if (!states.contains(transition.source()) || !states.contains(transition.target()) ) {
			throw new IllegalArgumentException("Invalid source of destination state.");
		}
		
		List<Transition> transitionsFromSource = transitions.get(transition.source());
		transitionsFromSource.add(transition);
	}

	@Override
	public void setInitialTransition(PrimitiveTransition initialTransition) {
		assertInactive("Initial transition setup is not allowed when state machine is active.");
		
		if (!states.contains(initialTransition.target())) {
			throw new IllegalArgumentException("Invalid default state - state not contained in the state machine.");
		}
		
		this.initialTransition = initialTransition; 
	}

	@Override
	public State activeState() {
		assertActive("State machine is inactive - no state is active at this stage, activate first.");
		
		return activeState;
	}

	private boolean isTransitionEnabled(Event event, Transition transition) {
		return transition.isTriggerableBy(event) 
				&& transition.evaluateGuardFor(event);
	}

	private void fireTransition(PrimitiveTransition transition) {
		activeState.onExit();
		transition.takeEffect();
		activeState = transition.target();
		activeState.onEntry();
		activeState.doAction();
	}

	@Override
	public void doActivate() {
		fireTransition(initialTransition);
	}
	
	@Override
	public void doDeactivate() {
		activeState.deactivate();
		activeState = State.NULL_STATE;
	}

	@Override
	public boolean consumeEvent(Event event) {
		if (activeState.consumeEvent(event)) {
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
	public void setFinalState(State finalState) {
		this.finalState = finalState;
		
	}

	@Override
	public boolean hasReachedFinalState() {
		return activeState == finalState;
	}

	@Override
	public StateMachine containingStateMachine() {
		return owner.containingStateMachine();
	}

}