package org.moomin.statemachine;

public interface State {

	/* 
	 * Null object design pattern.
	 */
	public static final State NULL_STATE = new NoBehaviourState() {
		@Override
		public boolean isPassThrough() {
			return true;
	}};
	
	public void onEntry();
	
	public void doAction();
	
	public void onExit();
	
	public boolean isPassThrough();

}
