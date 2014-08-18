package org.haedus.machines;

/**
 * Created with IntelliJ IDEA.
 * User: goats
 * Date: 9/10/13
 * Time: 7:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeFactory {

	private static int indexCounter = 0;

	private static final Node EMPTYNODE = new Node(0);
    private static final Node DEADSTATE = new Node(Integer.MIN_VALUE);

	public static Node getNode() {
		return getNode(false);
	}

	public static Node getNode(boolean accepting) {
		setIndexCounter(getIndexCounter() + 1);
		return new Node(getIndexCounter(), accepting);
	}

    public static Node getDeadState() { return DEADSTATE; }

	public static Node getEmptyNode() {
		return EMPTYNODE;
	}

	public static int getIndexCounter() {
		return indexCounter;
	}

	public static void setIndexCounter(int indexCounter) {
		NodeFactory.indexCounter = indexCounter;
	}

}
