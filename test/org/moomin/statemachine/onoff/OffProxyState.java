package org.moomin.statemachine.onoff;

import org.moomin.statemachine.State;

public class OffProxyState implements State {

	public OffProxyState(String string) {}

	@Override
	public void onEntry() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doAction() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onExit() {
		// TODO Auto-generated method stub

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
