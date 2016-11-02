package org.moomin.statemachine;

import java.util.List;

public abstract class State extends MultiplyActivatableObject 
	implements StateMachinePart, EventConsumer {

	private Region owningRegion;
	
	/* 
	 * Null object design pattern.
	 */
	public static final State NULL_STATE = new NoBehaviourSimpleState() {};
	
	
	public Transition selectTransitionToFire(List<Transition> outgoingTransitions, 
			Event event) {
		for (Transition transition : outgoingTransitions) {
			if( isTransitionEnabled(event, transition) ) {
				return transition;
			}
		}
		
		return null;
	}
	
	
	protected abstract void onEntryBehaviour(Event entryEvent);
	
	protected abstract void doActionBehaviour();
	
	protected abstract void doActionClose();
	
	protected abstract void onExitBehaviour();
	
	
	public final void onEntry(Event entryEvent) {
		onEntryBehaviour(entryEvent);
		activate();
	}
	
	public final void doAction() {
		doActionBehaviour();
		doActionClose();
	}

	public final void onExit() {
		deactivate();
		onExitBehaviour();
	}

	public final StateMachine containingStateMachine() {
		return owningRegion.containingStateMachine();
	}
	
	final void assignOwner(Region owningRegion) {
		this.owningRegion = owningRegion;
	}

	
	protected static boolean isTransitionEnabled(Event event, Transition transition) {
		return transition.isTriggerableBy(event) 
				&& transition.evaluateGuardFor(event);
	}

}
