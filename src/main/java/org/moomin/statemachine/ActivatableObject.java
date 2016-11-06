package org.moomin.statemachine;

public abstract class ActivatableObject implements Activatable {

	private boolean isActive = false;
	
	@Override
	public final void deactivate() {
		beforeDeactivation();
		isActive = false;
		doDeactivate();
	}

	@Override
	public final void activate() {
		beforeActivation();
		isActive = true;
		doActivate();
	}

	@Override
	public final boolean isActive() {
		return isActive;
	}

	protected abstract void doDeactivate();
	
	protected abstract void doActivate();
	
	protected abstract void beforeDeactivation();
	
	protected abstract void beforeActivation();
	
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
