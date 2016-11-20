package org.moomin.statemachine;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.core.StringContains.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.moomin.statemachine.taskrouter.RouteTaskState;
import org.moomin.statemachine.taskrouter.TaskAccomplishedEvent;
import org.moomin.statemachine.taskrouter.TaskReceivedEvent;

/**
 * 	TODO - consider extracting method for test choice state step
 */
@RunWith(MockitoJUnitRunner.class)
public class ChoiceStateStateMachineTest extends StateMachineTestBase {

	private static final int NUMBER_OF_NODES = 3;
	private State waitForTaskState;
	private State routeTaskState;
	private State publishResultsState;
	private List<State> nodeStates;
	private List<TransitionGuard> nodeNotBusyGuards;
	
	@Override
	@Before
	public void setUp() {
		super.setUp();
		
		createNonNodeStatesAndTransitions();
		createNodeStatesAndTransitionsIncludingGuards();
		
		setInitialTransitionAndActivate(waitForTaskState);
	}

	private void createNonNodeStatesAndTransitions() {
		waitForTaskState = addState(spy(State.class));
		routeTaskState = addState(new RouteTaskState("Routing"));
		publishResultsState = addState(spy(NoBehaviourSimpleState.class));
		addTransition(waitForTaskState, routeTaskState, TaskReceivedEvent.class);
		addCompletionTransition(publishResultsState, waitForTaskState);
	}

	private void createNodeStatesAndTransitionsIncludingGuards() {
		nodeStates = new ArrayList<>();
		nodeNotBusyGuards = new ArrayList<>();
		for (int i = 0; i < NUMBER_OF_NODES; ++i) {
			nodeStates.add(addState(spy(SimpleState.class)));
			addTransition(nodeStates.get(i), publishResultsState, TaskAccomplishedEvent.class);
		
			TransitionGuard nodeNotBusyGuard = mock(TransitionGuard.class); 
			nodeNotBusyGuards.add(nodeNotBusyGuard);
			
			addCompletionTransition(routeTaskState, nodeStates.get(i), nodeNotBusyGuard);
		}
	}

	
	@Test
	public void testChoiceState() {
		// Waiting -> Node1 - all nodes free
		setNodeNotBusyGuardsEvaluationResults(Arrays.asList(true, true, true));
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), nodeStates.get(0));
		// Node1 -> Publishing -> Waiting
		dispatchThenProcessEventAndCheckActiveState(new TaskAccomplishedEvent(), waitForTaskState);
		
		// Waiting -> Node2 - node 1 busy, nodes 2,3 free
		setNodeNotBusyGuardsEvaluationResults(Arrays.asList(false, true, true));
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), nodeStates.get(1));
		// Node2 -> Publishing -> Waiting
		dispatchThenProcessEventAndCheckActiveState(new TaskAccomplishedEvent(), waitForTaskState);
		
		// Waiting -> Node3 - nodes 1,2 busy, node 3 free
		setNodeNotBusyGuardsEvaluationResults(Arrays.asList(false, false, true));
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), nodeStates.get(2));
		// Node3 -> Publishing -> Waiting
		dispatchThenProcessEventAndCheckActiveState(new TaskAccomplishedEvent(), waitForTaskState);

		// Waiting -> Node2 - nodes 1,3 busy, node 2 got free
		setNodeNotBusyGuardsEvaluationResults(Arrays.asList(false, true, false));
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), nodeStates.get(1));
		// Node2 -> Publishing -> Waiting
		dispatchThenProcessEventAndCheckActiveState(new TaskAccomplishedEvent(), waitForTaskState);
		
		// Waiting -> Node1, node 2 busy, nodes 1,3 got free, 
		setNodeNotBusyGuardsEvaluationResults(Arrays.asList(true, false, true));
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), nodeStates.get(0));
		// Node1 -> Publishing -> Waiting
		dispatchThenProcessEventAndCheckActiveState(new TaskAccomplishedEvent(), waitForTaskState);
		
		// Waiting -> Node3, nodes 1,2 busy, node 3 free
		setNodeNotBusyGuardsEvaluationResults(Arrays.asList(false, false, true));
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), nodeStates.get(2));
		// Node3 -> Publishing -> Waiting
		dispatchThenProcessEventAndCheckActiveState(new TaskAccomplishedEvent(), waitForTaskState);
		
		// all nodes busy - illegal state
		exception.expect(IllegalStateException.class);
		exception.expectMessage(containsString("Ill formed state machine"));
		exception.expectMessage(containsString("choice state must have at least one guard evaluating to true"));
		setNodeNotBusyGuardsEvaluationResults(Arrays.asList(false, false, false));
		dispatchThenProcessEventAndCheckActiveState(new TaskReceivedEvent(), nodeStates.get(0));
	}
	
	private void setNodeNotBusyGuardsEvaluationResults(Collection<Boolean> evaluationResults) {
		int nodeNotBusyGuardIndex = 0;
		for (boolean evaluationResult : evaluationResults) {
			when(nodeNotBusyGuards.get(nodeNotBusyGuardIndex++).evaluate(any(), any())).thenReturn(evaluationResult);	
		}
	}
}
