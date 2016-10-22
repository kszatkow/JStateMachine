package org.moomin.statemachine;

public abstract class State {

	/* 
	 * Null object design pattern.
	 */
	public static final State NULL_STATE = new NoBehaviourState() {
		@Override
		public boolean isPassThrough() {
			return true;
		}
		
		@Override
		public boolean isComposite() {
			return false;
		}
	};
	
	
	public abstract boolean isPassThrough();
	
	public abstract boolean isComposite();

	
	public final void onEntry() {
		onEntryBehaviour();
	}
	
	public final void doAction() {
		doActionBehaviour();
	}
	
	public final void onExit() {
		onExitBehaviour();
	}


	protected abstract void onEntryBehaviour();
	
	protected abstract void doActionBehaviour();
	
	protected abstract void onExitBehaviour();

}
