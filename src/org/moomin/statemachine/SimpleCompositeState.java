package org.moomin.statemachine;

public abstract class SimpleCompositeState /*extends PrimitiveStateMachine*/ extends State implements Region {

	PrimitiveStateMachine itsRegion = new PrimitiveStateMachine();
	
	@Override
	public final boolean isPassThrough() {
		return false;
	}
	
	@Override
	public boolean isComposite() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#reset()
	 */
	@Override
	public void reset() {
//		deactivate();
		itsRegion.deactivate();
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#activate()
	 */
	@Override
	public void activate() {
		itsRegion.activate();
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#dispatchEvent(org.moomin.statemachine.Event)
	 */
	@Override
	public void dispatchEvent(Event event) {
		itsRegion.dispatchEvent(event);
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#processEvent()
	 */
	@Override
	public void processEvent() {
		itsRegion.processEvent();
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#setInitialTransition(org.moomin.statemachine.InitialTransition)
	 */
	@Override
	public void setInitialTransition(InitialTransition initialTransition) {
		itsRegion.setInitialTransition(initialTransition);
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#addTransition(org.moomin.statemachine.Transition)
	 */
	@Override
	public void addTransition(Transition transition) {
		itsRegion.addTransition(transition);
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#getActiveState()
	 */
	@Override
	public State getActiveState() {
		return itsRegion.getActiveState();
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#addState(org.moomin.statemachine.State)
	 */
	@Override
	public void addState(State substate) {
		itsRegion.addState(substate);
	}
}
