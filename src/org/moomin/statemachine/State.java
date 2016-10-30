package org.moomin.statemachine;

public abstract class State extends MultiplyActivatableObject 
	implements StateMachinePart, EventConsumer {

	private Region owningRegion;
	
	/* 
	 * Null object design pattern.
	 */
	public static final State NULL_STATE = new NoBehaviourSimpleState() {};
	
	
	protected abstract void onEntryBehaviour();
	
	protected abstract void doActionBehaviour();
	
	protected abstract void doActionClose();
	
	protected abstract void onExitBehaviour();
	
	
	public final void onEntry() {
		onEntryBehaviour();
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
	
}
