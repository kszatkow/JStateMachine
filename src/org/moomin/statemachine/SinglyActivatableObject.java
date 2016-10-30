package org.moomin.statemachine;

public abstract class SinglyActivatableObject extends ActivatableObject {

	@Override
	protected final void beforeDeactivation() {
		assertActive("Already inactive.");
	}

	@Override
	protected final void beforeActivation() {
		assertInactive("Already active");

	}

}
