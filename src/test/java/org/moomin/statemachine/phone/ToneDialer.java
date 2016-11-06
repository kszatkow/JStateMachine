package org.moomin.statemachine.phone;

public class ToneDialer {

	boolean isDialToneOn = false;
	
	public void startDialTone() {
		isDialToneOn = true;
	}

	public void stopDialTone() {
		isDialToneOn = false;
	}
	
	public boolean isDialToneOn() {
		return isDialToneOn;
	}
}
