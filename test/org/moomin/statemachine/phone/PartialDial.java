package org.moomin.statemachine.phone;

import org.moomin.statemachine.State;

public class PartialDial extends State {

	public PartialDial(String string) {}

	@Override
	public void onEntryBehaviour() {
		// TODO add number handling implementation
	}

	@Override
	public void doActionBehaviour() {
		// empty on purpose
	}

	@Override
	public void onExitBehaviour() {
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
