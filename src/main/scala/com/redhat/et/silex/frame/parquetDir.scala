/*
 * This file is part of the "silex" library of helpers for Apache Spark.
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

package com.redhat.et.silex.frame

import org.apache.spark.sql.{DataFrame, SQLContext}

object ParquetDir {
  import com.redhat.et.silex.util.DirUtils.readdir
  
  def loadParquetDir(sqlc: SQLContext, dir: String, repartition: Int = 0): DataFrame = {
    readdir(dir) map {file => sqlc.read.load(file)} reduce ((a, b) => a.unionAll(b))
  }
}

