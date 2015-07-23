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

package com.redhat.et.silex.feature.extractor

/** Provides conversions from Breeze vectors to [[FeatureSeq]], and vice versa.
  * {{{
  * import com.redhat.et.silex.feature.extractor.{ FeatureSeq, Extractor }
  * import com.redhat.et.silex.feature.extractor.breeze
  * import com.redhat.et.silex.feature.extractor.breeze.implicits._
  * import _root_.breeze.linalg.DenseVector
  *
  * val bv = new DenseVector(Array(1.0, 2.0))
  * val featureSeq = FeatureSeq(bv)
  * val bv2 = featureSeq.toBreeze
  * }}}
  */
package object breeze {}
