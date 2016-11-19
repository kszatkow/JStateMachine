package org.moomin.statemachine;

import static org.junit.Assert.assertSame;

import java.util.Collection;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

public class StateMachineTestBase {

	protected StateMachine stateMachine;
	protected Region stateMachineRegion;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() {
		stateMachine = new StateMachine();
		stateMachineRegion = new RegionStateMachine(stateMachine);
		stateMachine.addRegion(stateMachineRegion);
	}

	@After
	public void tearDown() {
		stateMachine = null;
		stateMachineRegion = null;
	}

	protected static State addSubstate(Region owningRegion, State substate) {
		owningRegion.addState(substate);
		return substate;
	}
	
	protected State addState(State state) {
		stateMachineRegion.addState(state);
		return state;
	}

	protected SimpleCompositeState addSimpleCompositeState(SimpleCompositeState state) {
		return (SimpleCompositeState) addState(state);
	}

	protected void addTransition(Transition transition) {
		stateMachineRegion.addTransition(transition);
	}

	protected Transition addTransition(State source, State target, Class<? extends Event> triggerableBy) {
		Transition transition = new Transition(source, target, triggerableBy);
		stateMachineRegion.addTransition(transition);
		return transition;
	}

	protected void addTransition(State source, State target, Collection<Class<? extends Event>> triggerableBy) {
		stateMachineRegion.addTransition(
				new Transition(source, target, triggerableBy));
	}

	protected void addTransition(State source, State target, Class<? extends Event> triggerableBy, TransitionEffect effect) {
		stateMachineRegion.addTransition(
				new Transition(source, target, triggerableBy, effect));
	}

	protected void addTransition(State source, State target, Collection<Class<? extends Event>> triggerableBy, TransitionEffect effect) {
		stateMachineRegion.addTransition(
				new Transition(source, target, triggerableBy, effect));
	}

	protected void addTransition(State source, State target, Class<? extends Event> triggerableBy, TransitionGuard guard) {
		stateMachineRegion.addTransition(new Transition(source, target, triggerableBy, guard));
	}

	protected void addTransition(State source, State target, Set<Class<? extends Event>> triggerableBy, TransitionGuard guard) {
		stateMachineRegion.addTransition(new Transition(source, target, triggerableBy, guard));
	}

	protected CompletionTransition addCompletionTransition(State source, State target) {
		CompletionTransition completionTransition = new CompletionTransition(source, target);
		stateMachineRegion.addTransition(completionTransition);
		return completionTransition;
	}

	protected void addCompletionTransition(State source, State target, TransitionGuard guard) {
		stateMachineRegion.addTransition(
				new CompletionTransition(source, target, guard));
		
	}

	protected void addCompletionTransition(State source, State target, TransitionEffect effect) {
		stateMachineRegion.addTransition(
				new CompletionTransition(source, target, effect));
		
	}

	protected void addCompletionTransition(State source, State target, TransitionGuard guard, TransitionEffect effect) {
		stateMachineRegion.addTransition(
				new CompletionTransition(source, target, guard, effect));
	}

	protected void setInitialTransitionAndActivate(State initialState) {
		setInitialTransition(
				initialState,
				new TransitionEffect() {
						@Override
						public void execute() {
							// empty on purpose
						}
				});
		stateMachine.activate();
	}

	protected void setInitialTransition(State target, TransitionEffect effect) {
		stateMachineRegion.setInitialTransition(new PrimitiveTransition(target, effect));
	}

	protected void dispatchThenProcessEventAndCheckActiveState(Event event, State expectedState) {
		stateMachine.dispatchEvent(event);
		stateMachine.processEvent();
		assertSame(expectedState, stateMachineRegion.activeState());
	}

	protected void dispatchTwiceThenProcessAndCheckActiveState(Event event, State expectedActiveState) {
		stateMachine.dispatchEvent(event);
		dispatchThenProcessEventAndCheckActiveState(event, expectedActiveState);
	}

}