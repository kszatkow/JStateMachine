package org.moomin.statemachine;

public interface Activatable {

	void deactivate();

	void activate();

	boolean isActive();
	
}
