package org.haedus.machines;

import org.haedus.datatypes.ParseDirection;
import org.haedus.datatypes.phonetic.Sequence;
import org.haedus.datatypes.phonetic.SequenceFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by samantha on 12/28/14.
 */
public class StateMachineTest {

	private static final transient Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

	private static final SequenceFactory FACTORY = SequenceFactory.getEmptyFactory();

	@Test
	public void testBasicStateMachine01() {
		Node<Sequence> stateMachine = getMachine("a");

		test(stateMachine, "a");
		test(stateMachine, "aa");

		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testBasicStateMachine02() {
		Node<Sequence> stateMachine = getMachine("aaa");

		test(stateMachine, "aaa");

		fail(stateMachine, "a");
		fail(stateMachine, "aa");
		fail(stateMachine, "b");
		fail(stateMachine, "c");
	}

	@Test
	public void testStateMachineStar() {
		Node<Sequence> stateMachine = getMachine("aa*");

		fail(stateMachine, "a");
		test(stateMachine, "aa");
		test(stateMachine, "aaa");
		test(stateMachine, "aaaa");
		test(stateMachine, "aaaaa");
		test(stateMachine, "aaaaaa");
	}

	@Test
	public void testStateMachinePlus() {
		Node<Sequence> stateMachine = getMachine("a+");

		test(stateMachine, "a");
		test(stateMachine, "aa");
		test(stateMachine, "aaa");
		test(stateMachine, "aaaa");
		test(stateMachine, "aaaaa");
		test(stateMachine, "aaaaaa");

		test(stateMachine, "ab");
	}

	@Test
	public void testGroups() {
		Node<Sequence> stateMachine = getMachine("(ab)(cd)(ef)");

		test(stateMachine, "abcdef");
		fail(stateMachine, "abcd");
		fail(stateMachine, "ab");
		fail(stateMachine, "bcdef");
	}

	@Test
	public void testGroupStar01() {
		Node<Sequence> stateMachine = getMachine("(ab)*(cd)(ef)");

		test(stateMachine, "abababcdef");
		test(stateMachine, "ababcdef");
		test(stateMachine, "abcdef");
		test(stateMachine, "cdef");

		fail(stateMachine, "abcd");
		fail(stateMachine, "ab");
		fail(stateMachine, "bcdef");
		fail(stateMachine, "abbcdef");
	}

	@Test
	public void testGroupStar02() {
		Node<Sequence> stateMachine = getMachine("d(eo*)*b");

		test(stateMachine, "db");
		test(stateMachine, "deb");
		test(stateMachine, "deeb");
		test(stateMachine, "deob");
		test(stateMachine, "deoob");
		test(stateMachine, "deoeob");
		test(stateMachine, "deoeoob");

		fail(stateMachine, "abcd");
		fail(stateMachine, "ab");
		fail(stateMachine, "bcdef");
		fail(stateMachine, "abbcdef");
	}

	@Test
	public void testGroupOptional01() {
		Node<Sequence> stateMachine = getMachine("(ab)?(cd)(ef)");

		test(stateMachine, "abcdef");
		test(stateMachine, "cdef");
	}

	@Test
	public void testSets01() {
		Node<Sequence> stateMachine = getMachine("{ x ɣ }");

		test(stateMachine, "x");
		test(stateMachine, "ɣ");
		fail(stateMachine, " ");
	}

	@Test
	public void testSets02() {
		Node<Sequence> stateMachine = getMachine("{ab {cd xy} ef}tr");

		test(stateMachine, "abtr");
		test(stateMachine, "cdtr");
		test(stateMachine, "xytr");
		test(stateMachine, "eftr");
		fail(stateMachine, " ");
	}

	@Test
	public void testGroupPlus01() {
		Node<Sequence> machine = getMachine("(ab)+");

		test(machine, "ab");
		test(machine, "abab");
		test(machine, "ababab");

	}

	@Test
	public void testComplexGroups01() {
		Node<Sequence> machine = getMachine("(a+l(ham+b)*ra)+");

		test(machine, "alhambra");
	}

	@Test
	public void testComplexGroups02() {
		Node<Sequence> machine = getMachine("{ab* (cd?)+ ((ae)*f)+}tr");

		test(machine, "abtr");
		test(machine, "cdtr");
	}

	@Test
	public void testComplex01() {
		Node<Sequence> machine = getMachine("{r l}?{a e o ā ē ō}{i u}?{n m l r}?{pʰ tʰ kʰ ḱʰ}us");

		test(machine, "āḱʰus");
	}

	private static Node<Sequence> getMachine(String expression) {
		return NodeFactory.getStateMachine(expression, FACTORY, ParseDirection.FORWARD, true);
	}

	private static void test(Node<Sequence> stateMachine, String target) {
		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertFalse("Machine failed to accept input", matchIndices.isEmpty());
	}

	private static void fail(Node<Sequence> stateMachine, String target) {
		Collection<Integer> matchIndices = testMachine(stateMachine, target);
		assertTrue("Machine accepted input it should not have", matchIndices.isEmpty());
	}

	private static Collection<Integer> testMachine(Node<Sequence> stateMachine, String target) {
		Sequence sequence = FACTORY.getSequence(target);
		Collection<Integer> matchIndices = stateMachine.getMatchIndices(0, sequence);
		LOGGER.debug("{} ran against \"{}\" and produced output {}",stateMachine, sequence, matchIndices);
		return matchIndices;
	}
}
