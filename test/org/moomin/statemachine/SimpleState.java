package org.moomin.statemachine;

public abstract class SimpleState extends State {

	@Override
	public boolean isPassThrough() {
		return false;
	}

	@Override
	public boolean isComposite() {
		return false;
	}

	@Override
	public final void activate() {}

	@Override
	public final void deactivate() {}

}
