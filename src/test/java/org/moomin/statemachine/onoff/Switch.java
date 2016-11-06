package org.moomin.statemachine.onoff;

public class Switch {

	private boolean isOn = false;
	
	public boolean isOn() {
		return isOn;
	}
	
	public void turnOn() {
		isOn = true;
	}
	
	public void turnOff() {
		isOn = false;
	}
	
}
