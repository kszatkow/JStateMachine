package org.moomin.statemachine.phone;

import org.moomin.statemachine.State;

public class StartDialing implements State {

	private ToneDialer toneDialer = new ToneDialer();
	
	public StartDialing(String string) {}

	@Override
	public void onEntry() {
		toneDialer.startDialTone();
	}

	@Override
	public void doAction() {
		// empty on purpose
	}

	@Override
	public void onExit() {
		toneDialer.stopDialTone();
	}

	@Override
	public boolean isPassThrough() {
		return false;
	}

	@Override
	public boolean isComposite() {
		return false;
	}

	public boolean isDialToneOn() {
		return toneDialer.isDialToneOn();
	}
}
