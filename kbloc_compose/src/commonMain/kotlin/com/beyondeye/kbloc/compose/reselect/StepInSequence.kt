package com.beyondeye.kbloc.compose.reselect

/**
 * In reduks we "react" to changes of the reduks state and perform actions.
 * If there is a sequence of actions, then we listen for the changes in the state that
 * trigger each step in the sequence SEPARATELY and it can be difficult to understand/mantain
 * such chain of actions, or even understand that  such chain exists at all,unless we analyze
 * thoroughly each of the onChangeIn clauses in a [StoreSubscriber] code
 * the [StepInSequence] class purpose is to make this chain of action more explicit and easier to
 * identify, and also help to avoid triggering by mistake a step of the chain before the previous
 * steps in the chain have been completed.
 * How to use: for each of such logic sequences that exist in your application, define a [StepInSequence]
 * field in the reduks state. Then, instead of listening to change in the reduks state with
 * [AbstractSelector.onChangeIn], use instead [AbstractSelector.onChangeAtStep]
 */
public class StepInSequence(public val nsteps:Int=1000, public val curstep:Int=-1) {
    /**
     * use this method in State reducer, to restart the [StepInSequence]
     */
    public fun restarted(startStep:Int=0):StepInSequence = StepInSequence(nsteps,startStep)
    /**
     * use this method in State reducer, to set the  [StepInSequence] as completed
     */
    public fun completed():StepInSequence= StepInSequence(nsteps,nsteps)
    /**
     * use this method in State reducer, to advance to next step in the [StepInSequence]
     */
    public fun withNextStep():StepInSequence = StepInSequence(nsteps,(curstep+1).coerceAtMost(nsteps))
    public fun withStep(newstep:Int):StepInSequence = StepInSequence(nsteps,newstep.coerceAtMost(nsteps))
    public fun isStarted():Boolean = curstep>=0
    public fun isCompleted():Boolean = curstep==nsteps
    public fun isRunning():Boolean = curstep>=0 && curstep<nsteps
}