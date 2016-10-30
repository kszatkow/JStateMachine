package org.moomin.statemachine;

public abstract class ActivatableObject implements Activatable {

	private boolean isActive = false;
	
	@Override
	public void deactivate() {
		doDeactivate();
		isActive = false;
	}

	@Override
	public void activate() {
		doActivate();
		isActive = true;
	}
	
	@Override
	public boolean isActive() {
		return isActive;
	}

	protected abstract void doDeactivate();
	
	protected abstract void doActivate();
	
	protected void assertInactive(String exceptionMessage) {
		if (isActive()) {
			throw new IllegalStateException(exceptionMessage);
		}
	}
	
	protected void assertActive(String exceptionMessage) {
		if (!isActive()) {
			throw new IllegalStateException(exceptionMessage);
		}
	}
	
}
