package org.moomin.statemachine;

public abstract class State {

	/*
	 * TODO think how to fix it - dummy solution for now
	 */
	protected Region owningRegion;
	
	/* 
	 * Null object design pattern.
	 */
	public static final State NULL_STATE = new NoBehaviourState() {
		@Override
		public boolean isPassThrough() {
			return true;
		}
	};
	
	
	public abstract boolean isPassThrough();
	
	public abstract boolean tryConsumingEvent(Event event);
	
	public abstract void activate();

	public abstract void deactivate();
	
	protected abstract void onEntryBehaviour();
	
	protected abstract void doActionBehaviour();
	
	protected abstract void onExitBehaviour();
	
	
	public final void onEntry() {
		activate();
		onEntryBehaviour();
	}
	
	public final void doAction() {
		doActionBehaviour();
		owningRegion.dispatchInternalEvent(new CompletionEvent());
	}
	
	public final void onExit() {
		onExitBehaviour();
		deactivate();
	}

}
