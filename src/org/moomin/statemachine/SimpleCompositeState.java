package org.moomin.statemachine;

public abstract class SimpleCompositeState extends PrimitiveStateMachine implements State {

	@Override
	public final boolean isPassThrough() {
		return false;
	}
	
	@Override
	public boolean isComposite() {
		return true;
	}

	public void reset() {
		deactivate();
	}
}
