/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hbase.spark

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.spark.sql._
import org.apache.spark.sql.sources.TableScan

case class HBaseRelation(tableName: String, schemaDefine: String, hbaseSite: String)(@transient val sqlContext: SQLContext) extends TableScan {

  val hbaseSchema = HBaseSchema.parseFrom(schemaDefine)

  val schema = {
    hbaseSchema.toStructType()
  }

  lazy val buildScan = {
    val conf = HBaseConfiguration.create()
    conf.set(TableInputFormat.INPUT_TABLE, tableName)
    val hbaseRDD = sqlContext.sparkContext.newAPIHadoopRDD(conf, classOf[TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[org.apache.hadoop.hbase.client.Result])
    hbaseRDD.map { record =>
      val values = HBaseRecord(hbaseSchema, record._2).values()
      Row.fromSeq(values)
    }
  }
}

