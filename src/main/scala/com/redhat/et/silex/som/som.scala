/*
 * som.scala
 * 
 * author:  William Benton <willb@redhat.com>
 *
 * Copyright (c) 2016 Red Hat, Inc.
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
 * limitations under the License.
 */

package com.redhat.et.silex.som

import com.redhat.et.silex.util.SampleSink

import breeze.linalg._
import breeze.numerics._

object Neighborhood {
  private [this] def gaussian(distance: Double, sigma: Double): Double = math.exp(- (distance * distance) / (sigma * sigma))
  
  /** Returns a <tt>dim</tt>-element vector of the values for the one-dimensional Gaussian neighborhood function, centered at <tt>c</tt> */
  def vec(c: Int, dim: Int, sigma: Double): DenseVector[Double] = {
    // weights for each distance
    val weights = (0 to math.max(c, math.abs(dim - c))).map { x => gaussian(x.toDouble, sigma) }
    DenseVector((0 until dim).map { x => weights(math.abs(x - c))}.toArray)
  }
  
  /** Returns a <tt>xdim</tt>*<tt>ydim</tt>-element matrix of the values for the two-dimensional Gaussian neighborhood function, centered at <tt>xc</tt>, <tt>yc</tt> */
  def mat(xc: Int, xdim: Int, xsigma: Double, yc: Int, ydim: Int, ysigma: Double): DenseMatrix[Double] = {
    vec(yc, ydim, ysigma) * vec(xc, xdim, xsigma).t
  }
}

class SOM(val xdim: Int, val ydim: Int, val fdim: Int, _entries: DenseVector[DenseVector[Double]], private val mqsink: SampleSink) extends Serializable {
  import breeze.numerics._
  import org.apache.spark.mllib.linalg.{Vector=>SV, DenseVector=>SDV, SparseVector=>SSV}
  
  val entries = _entries.toArray
  
  val norms = entries.zip(entries.map(norm(_))).zipWithIndex
  
  def trainingMatchQuality = { SampleSink.empty += mqsink }
  
  def closest(example: Vector[Double], exampleNorm: Option[Double] = None): Int = {
    (closestWithSimilarity(example, exampleNorm))._1
  }
  
  /** Return the index of the most similar vector in the map to the supplied example, along with its similarity */
  def closestWithSimilarity(example: Vector[Double], exampleNorm: Option[Double] = None): (Int, Double) = {
    val vn = exampleNorm.getOrElse(norm(example))
    val ce = norms(0)
    val initialCandidate = (0, math.min(1.0, math.max(-1.0, (example dot ce._1._1) / (ce._1._2 * vn))))
    
    norms.foldLeft(initialCandidate) { 
      case(answer: (Int, Double), ((e: Vector[Double], en: Double), i: Int)) => {
        val candidate = (i, math.min(1.0, math.max(-1.0, (example dot e) / (en * vn))))
        if (answer._2 > candidate._2) answer else candidate
      }
    }
  }
  
  def closestWithSimilarity(example: SV, exampleNorm: Option[Double]): (Int, Double) = {
    closestWithSimilarity(SOM.spark2breeze(example), exampleNorm)
  }
}

object SOM {
  import breeze.numerics._
  import org.apache.spark.rdd.RDD
  import org.apache.spark.mllib.linalg.{Vector=>SV, DenseVector=>SDV, SparseVector=>SSV}

  import com.esotericsoftware.kryo.Kryo
  import com.esotericsoftware.kryo.io.{Input, Output}
  import org.objenesis.strategy.StdInstantiatorStrategy
  import scala.util.Try

  def save(som: SOM, path: String): Try[Unit] = {
    Try({
      val k = new Kryo()
      val out = new Output(new java.io.FileOutputStream(path))
      k.writeClassAndObject(out, som)
      ()
    })
  }

  def load(path: String): Try[SOM] = {
    Try({
      val k = new Kryo()
      val input = new Input(new java.io.FileInputStream(path))
      k.setInstantiatorStrategy(new StdInstantiatorStrategy())
      k.readClassAndObject(input).asInstanceOf[SOM]
    })
  }

  private [som] case class SomTrainingState(counts: Array[Int], weights: Array[DenseVector[Double]], mqsink: SampleSink) {
    /* destructively updates this state with a new example */
    def update(index: Int, example: Vector[Double]): SomTrainingState = {
      counts(index) = counts(index) + 1
      weights(index) = weights(index) + example
      this
    }
    
    /* destructively updates the arrays in this state with a new example; maybe creates a new wrapper to hold a new worst similarity */
    def update(indexAndSimilarity: (Int, Double), example: Vector[Double]): SomTrainingState = {
      val (index, similarity) = indexAndSimilarity
      mqsink.put(similarity)
      this.update(index, example)
    }
    
    /* destructively merges other into this */
    def combine(other: SomTrainingState) = { 
      (0 until counts.length) foreach { index =>
        this.counts(index) = this.counts(index) + other.counts(index)
        this.weights(index) = this.weights(index) + other.weights(index)
      }
      
      this.mqsink += other.mqsink
      this
    }
    
    def matchQuality = SampleSink.empty += mqsink
  }
  
  private [som] object SomTrainingState {
    def empty(dim: Int, fdim: Int): SomTrainingState = SomTrainingState(Array.fill(dim)(0), Array.fill(dim)(DenseVector.zeros[Double](fdim)), SampleSink.empty)
  }
  
  /** Initialize a self-organizing map with random weights, <tt>xdim</tt> columns, <tt>ydim</tt> rows, and <tt>fdim</tt> features per cell. */
  def random(xdim: Int, ydim: Int, fdim: Int, seed: Option[Int] = None): SOM = {
    // nb: could/should use breeze PRNGs?
    val rng = seed.map { s => new scala.util.Random(s) }.getOrElse(new scala.util.Random())
    val randomMap = DenseVector.fill[DenseVector[Double]](xdim * ydim)(DenseVector.fill[Double](fdim)(rng.nextDouble()))

    new SOM(xdim, ydim, fdim, randomMap, SampleSink.empty)
  }
  
  /** Create a new SOM instance with the results of the training state */
  private [som] def step(xdim: Int, ydim: Int, fdim: Int, state: SomTrainingState, xsigma: Double, ysigma: Double, lastState: SOM) = {
    var weights = DenseVector.fill[DenseVector[Double]](xdim * ydim)(DenseVector.zeros[Double](fdim))
    val seenWeights = DenseVector[DenseVector[Double]](state.weights)
    var neighborhoods = DenseVector.zeros[Double](xdim * ydim)
    
    (0 until xdim * ydim).foreach { idx =>
      val (xc, yc) = (idx / ydim, idx % ydim)
      val counts = state.counts(idx).toDouble
      val hood = Neighborhood.mat(xc, xdim, xsigma, yc, ydim, ysigma).reshape(xdim * ydim, 1).toDenseVector
      neighborhoods = neighborhoods :+ (hood * counts)
      val update = DenseVector(hood.toArray.map { hoodDistance => state.weights(idx) * hoodDistance})
      weights = weights :+ update
    }
    
    val newWeights = DenseVector((weights.values.iterator.zip(lastState.entries.iterator)).zip(neighborhoods.values.iterator).map { 
      case ((vd, od), d) if d == 0.0 => od
      case ((vd, _), d) => vd / d 
    }.toArray)
    new SOM(xdim, ydim, fdim, newWeights, state.matchQuality)
  }
  
  @inline private [som] def spark2breeze(vec: SV): Vector[Double] = {
    vec match {
      case sv: SSV => {
        val vb = new VectorBuilder[Double](sv.size)
        (sv.indices zip sv.values).foreach { case (idx: Int, v: Double) =>
          vb.add(idx, v)
        }
        vb.toSparseVector()
      }
      
      case dv: SDV => {
        DenseVector(dv.toArray)
      }
    }
  }
  
  /** Train a self-organizing map from an RDD of <tt>examples</tt> for <tt>iterations</tt> iterations, starting with a randomly-weighted map with <tt>xdim</tt> columns, <tt>ydim</tt> rows, and <tt>fdim</tt> features per cell. Optional parameters include <tt>sigmaScale</tt>, to specify the width of the neighborhood function as a factor of each map dimension, <tt>minSigma</tt>, which sets the neighborhood width at the last iteration, and <tt>hook</tt>, which is a callback function to run after each iteration. */
  def train(xdim: Int, ydim: Int, fdim: Int, iterations: Int, examples: RDD[SV], seed: Option[Int] = None, sigmaScale: Double = 0.95, minSigma: Double = 1, hook: (Int, SOM) => Unit = { case (_,_) => }): SOM = {
    val xSigmaStep = ((xdim * sigmaScale) - minSigma) / iterations
    val ySigmaStep = ((ydim * sigmaScale) - minSigma) / iterations
    val sc = examples.context
    val normedExamples = examples.map { 
      ex => {
        val bv = spark2breeze(ex)
        (bv, norm(bv))
      }
    }.cache
    
    val startState = random(xdim, ydim, fdim, seed)
    
    hook(0, startState)
    
    (0 until iterations).foldLeft(startState) { (acc, it) =>
      val xSigma = (xdim * sigmaScale) - (xSigmaStep * it)
      val ySigma = (ydim * sigmaScale) - (ySigmaStep * it)
      val currentSOM = sc.broadcast(acc)
      
      val newState = normedExamples.treeAggregate(SomTrainingState.empty(xdim * ydim, fdim))(
        {case (state, (example: Vector[Double], n: Double)) => state.update(currentSOM.value.closestWithSimilarity(example, Some(n)), example)}, 
        {case (s1, s2) => s1.combine(s2)}
      )
      
      currentSOM.unpersist
      
      val nextSOM = step(xdim, ydim, fdim, newState, xSigma, ySigma, acc)
      hook(it + 1, nextSOM)
      nextSOM
    }
  }
}
