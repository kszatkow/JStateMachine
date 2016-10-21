package org.moomin.statemachine;

public class ChoiceState extends NoBehaviourState {

	public ChoiceState(String string) {	}

	@Override
	public boolean isPassThrough() {
		return true;
	}

	@Override
	public boolean isComposite() {
		return false;
	}

}
