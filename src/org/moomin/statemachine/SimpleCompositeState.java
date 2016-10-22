package org.moomin.statemachine;

public abstract class SimpleCompositeState extends State implements Region {

	private PrimitiveStateMachine ownedRegion = new PrimitiveStateMachine();
	
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
	public void deactivate() {
		ownedRegion.deactivate();
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#activate()
	 */
	@Override
	public void activate() {
		ownedRegion.activate();
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#dispatchEvent(org.moomin.statemachine.Event)
	 */
	@Override
	public void dispatchEvent(Event event) {
		ownedRegion.dispatchEvent(event);
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#processEvent()
	 */
	@Override
	public void processEvent() {
		ownedRegion.processEvent();
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#setInitialTransition(org.moomin.statemachine.InitialTransition)
	 */
	@Override
	public void setInitialTransition(InitialTransition initialTransition) {
		ownedRegion.setInitialTransition(initialTransition);
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#addTransition(org.moomin.statemachine.Transition)
	 */
	@Override
	public void addTransition(Transition transition) {
		ownedRegion.addTransition(transition);
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#getActiveState()
	 */
	@Override
	public State getActiveState() {
		return ownedRegion.getActiveState();
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#addState(org.moomin.statemachine.State)
	 */
	@Override
	public void addState(State substate) {
		ownedRegion.addState(substate);
	}
	
	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#dispatchInternalEvent(org.moomin.statemachine.Event)
	 */
	@Override
	public void dispatchInternalEvent(Event event) {
		ownedRegion.dispatchInternalEvent(event);
	}
}
