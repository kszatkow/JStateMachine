package org.moomin.statemachine;

public interface State {

	/* 
	 * Null object design pattern.
	 */
	public static final State NULL_STATE = new NoBehaviourState() {};
	
	public void onEntry();
	
	public void doAction();
	
	public void onExit();
}
