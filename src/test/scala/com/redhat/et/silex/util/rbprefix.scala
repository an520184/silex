/*
 * This file is part of the "silex" library of helpers for Apache Spark.
 *
 * Copyright (c) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.c
 */

package com.redhat.et.silex.util.rbprefix

import org.scalatest._

import com.redhat.et.silex.testing.matchers._

object RBNodeProperties extends FlatSpec with Matchers {
  trait Color
  case object Red extends Color
  case object Black extends Color

  def color[K, V](node: RBNode[K, V]): Color = node match {
    case n: RNode[K, V] => Red
    case _ => Black
  }

  def blackHeight[K, V](node: RBNode[K, V]): Set[Int] = node match {
    case Leaf() => Set(1)
    case RNode(_, _, lnode, rnode) => blackHeight(lnode) ++ blackHeight(rnode)
    case BNode(_, _, lnode, rnode) => (blackHeight(lnode) ++ blackHeight(rnode)).map(_ + 1)
  }

  def testBlackHeight[K, V](node: RBNode[K, V]): Boolean = node match {
    case Leaf() => true

    case RNode(_, _, lnode, rnode) =>
      testBlackHeight(lnode) && testBlackHeight(rnode) &&
      (blackHeight(lnode) == blackHeight(rnode)) && (blackHeight(lnode).size == 1)
      
    case BNode(_, _, lnode, rnode) =>
      testBlackHeight(lnode) && testBlackHeight(rnode) &&
      (blackHeight(lnode) == blackHeight(rnode)) && (blackHeight(lnode).size == 1)
  }

  def testRedChildrenBlack[K, V](node: RBNode[K, V]): Boolean = node match {
    case Leaf() => true
    case n: RNode[K, V] => color(n.lnode) == Black && color(n.rnode) == Black &&
      testRedChildrenBlack(n.lnode) && testRedChildrenBlack(n.rnode)
    case n: BNode[K, V] => testRedChildrenBlack(n.lnode) && testRedChildrenBlack(n.rnode)
  }

  def balance[K, V](node: RBNode[K, V]): (Int, Int) = node match {
    case Leaf() => (0, 0)
    case n: RNode[K, V] => {
      val (lmin, lmax) = balance(n.lnode)
      val (rmin, rmax) = balance(n.rnode)
      (1 + math.min(lmin, rmin), 1 + math.max(lmax, rmax))
    }
    case n: BNode[K, V] => {
      val (lmin, lmax) = balance(n.lnode)
      val (rmin, rmax) = balance(n.rnode)
      (1 + math.min(lmin, rmin), 1 + math.max(lmax, rmax))
    }
  }

  def testBalance[K, V](node: RBNode[K, V]): Boolean = node match {
    case Leaf() => true
    case n: RNode[K, V] => {
      val (pmin, pmax) = balance(node)
      (pmax <= (2 * pmin)) && testBalance(n.lnode) && testBalance(n.rnode)
    }
    case n: BNode[K, V] => {
      val (pmin, pmax) = balance(node)
      (pmax <= (2 * pmin)) && testBalance(n.lnode) && testBalance(n.rnode)
    }
  }

  // test RB tree invariant properties related to RB construction
  def testRB[K, V](root: RBNode[K, V]) = {
    // The root node of a RB tree should be black
    color(root) should be (Black)

    // Every path from a node to its descendant leafs should contain the same # of black nodes
    testBlackHeight(root) should be (true)

    // If a node is red, then both its children should be black
    testRedChildrenBlack(root) should be (true)

    // Depth of deepest node should be <= twice the depth of shallowest
    testBalance(root) should be (true)
  }
}


class RBMapSpec extends FlatSpec with Matchers {
  import RBNodeProperties._

  it should "have valid empty object" in {
    testRB(RBMap.empty[String, Int])
    testRB(RBMap.empty[Int, Char])
  }

  it should "insert initial node" in {
    testRB(RBMap.empty[String, Int] + (("a", 1)))
    testRB(RBMap.empty[Int, Char] + ((1, 'a')))
  }

  it should "build from data" in {
    val data = Vector.tabulate(100)(j => (j, j.toString))
    (1 to 1000).foreach { u =>
      val shuffled = scala.util.Random.shuffle(data)
      val rbmap = RBMap(shuffled:_*)

      // verify R/B tree construction invariants
      testRB(rbmap)

      // verify the map elements are ordered by key
      rbmap.iterator.map(_._1).toSeq should beEqSeq(data.map(_._1))

      // verify the map correctly preserves key -> value mappings
      data.map(x => rbmap.get(x._1)) should beEqSeq(data.map(x => Option(x._2)))
    }
  }
}
