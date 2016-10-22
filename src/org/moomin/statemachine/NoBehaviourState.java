package org.moomin.statemachine;

/** 
 * Null object, designed for inheritance only. 
 * See State class for publicly available instance of this null object. 
 */
public abstract class NoBehaviourState extends SimpleState {

	protected NoBehaviourState() {}
	
	@Override
	public final void onEntryBehaviour() {
		// empty on purpose
	}

	@Override
	public final void doActionBehaviour() {
		// empty on purpose
	}

	@Override
	public final void onExitBehaviour() {
		// empty on purpose
	}

}
