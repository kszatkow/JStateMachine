package org.moomin.statemachine;

/** 
 * Null object, designed for inheritance only. 
 * See State class for publicly available instance of this null object. 
 */
public class NoBehaviourState implements State {

	protected NoBehaviourState() {}
	
	@Override
	public void onEntry() {
		// empty on purpose
	}

	@Override
	public void doAction() {
		// empty on purpose
	}

	@Override
	public void onExit() {
		// empty on purpose
	}

}
