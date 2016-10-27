package org.moomin.statemachine;

public abstract class State {

	/*
	 * TODO think how to fix it - dummy solution for now
	 */
	protected Region owningRegion;
	
	/* 
	 * Null object design pattern.
	 */
	public static final State NULL_STATE = new NoBehaviourSimpleState() {};
	
	
	public abstract boolean tryConsumingEvent(Event event);
	
	public abstract void activate();

	public abstract void deactivate();
	
	protected abstract void onEntryBehaviour();
	
	protected abstract void doActionBehaviour();
	
	protected abstract void onExitBehaviour();
	
	
	public final void onEntry() {
		onEntryBehaviour();
		activate();
	}
	
	// TODO - how to solve it better
	public abstract void doAction();
	
	public final void onExit() {
		deactivate();
		onExitBehaviour();
	}

}
