/*******************************************************************************
 * Copyright (c) 2015. Samantha Fiona McCabe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.haedus.machines;

import java.util.Collection;

/**
 * Created by Samantha F M McCabe on 12/21/14.
 */
public interface Node<T> {

    /**
     * Checks if the object has any outgoing connections to other nodes
     * @return true iff
     */
    boolean isTerminal();

    /**
     *
     *
     * @param startIndex
     * @param target
     * @return
     */
    boolean matches(int startIndex, T target);

    /**
     * Checks if the current node contains another state machine inside it
     * @return true iff this node contains a state machine
     */
    boolean containsStateMachine();

    /**
     *
     * @param startIndex
     * @param target
     * @return
     */
    Collection<Integer> getMatchIndices(int startIndex, T target);

    /**
     * Adds a single node on a blank arc
     * @param node the node to add; must not be null
     */
    void add(Node<T> node);

    /**
     * Adds an outgoing connection to another node
     * @param arcValue the label of the outgoing arc
     * @param node the node to connect to
     */
    void add(T arcValue, Node<T> node);

    /**
     * Checks for the presence of any outgoing arcs with the provided labels
     * @param arcValue the label value to test for
     * @return true iff an arc exists with the label provided
     */
    boolean hasArc(T arcValue);

    /**
     * Returns all nodes reachable from the curent node by an arc with the provided label
     * @param arcValue
     * @return
     */
    Collection<Node<T>> getNodes(T arcValue);

    /**
     * Returns a set containing labels used in outgoing arcs; may contain null
     * @return a set of objects representing the labels of outgoing arcs; the set may contain null, but cannot be null itself.
     */
    Collection<T> getKeys();

    /**
     * Each node should have an id associated with it; this method returns it
     * @return the node's id code
     */
    String getId();

    boolean isAccepting();
}
