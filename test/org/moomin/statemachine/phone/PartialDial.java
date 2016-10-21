package org.moomin.statemachine.phone;

import org.moomin.statemachine.State;

public class PartialDial implements State {

	public PartialDial(String string) {}

	@Override
	public void onEntry() {
		// TODO add number handling implementation
	}

	@Override
	public void doAction() {
		// empty on purpose
	}

	@Override
	public void onExit() {
		// empty on purpose
	}

	@Override
	public boolean isPassThrough() {
		return false;
	}

	@Override
	public boolean isComposite() {
		return false;
	}

}
