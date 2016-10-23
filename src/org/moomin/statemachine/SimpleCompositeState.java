package org.moomin.statemachine;

public abstract class SimpleCompositeState extends State {

	private PrimitiveStateMachine ownedRegion;
	
	public SimpleCompositeState(StateMachine owningStateMachine) {
		ownedRegion = new PrimitiveStateMachine(owningStateMachine);
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
	 * @see org.moomin.statemachine.Region#setInitialTransition(org.moomin.statemachine.InitialTransition)
	 */
	public void setInitialTransition(InitialTransition initialTransition) {
		ownedRegion.setInitialTransition(initialTransition);
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#addTransition(org.moomin.statemachine.Transition)
	 */
	public void addTransition(Transition transition) {
		ownedRegion.addTransition(transition);
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#getActiveState()
	 */
	public State getActiveState() {
		return ownedRegion.getActiveState();
	}

	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#addState(org.moomin.statemachine.State)
	 */
	public void addState(State substate) {
		ownedRegion.addState(substate);
	}
	
	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#dispatchInternalEvent(org.moomin.statemachine.Event)
	 */
	public void dispatchInternalEvent(Event event) {
		owningRegion.dispatchInternalEvent(event);
	}
	
	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#tryConsumingEvent(org.moomin.statemachine.Event)
	 */
	@Override
	public boolean tryConsumingEvent(Event event) {
		return ownedRegion.tryConsumingEvent(event);
	}
	
	/* (non-Javadoc)
	 * @see org.moomin.statemachine.Region#isActive()
	 */
	public boolean isActive() {
		return ownedRegion.isActive();
	}
}
