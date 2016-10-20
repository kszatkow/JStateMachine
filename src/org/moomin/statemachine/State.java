package org.moomin.statemachine;

public interface State {

	/* 
	 * Null object design pattern.
	 */
	public static final State NULL_STATE = new NoBehaviourState() {
		@Override
		public boolean consumesEvent(Event event) {
			return false;
	}};
	
	public void onEntry();
	
	public void doAction();
	
	public void onExit();

	public boolean consumesEvent(Event event);
}
