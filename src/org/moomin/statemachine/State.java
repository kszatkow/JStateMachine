package org.moomin.statemachine;

public interface State {
	
	public void onEntry();
	
	public void doAction();
	
	public void onExit();
}
