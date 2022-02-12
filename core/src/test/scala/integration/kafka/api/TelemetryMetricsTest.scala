/**
  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
  * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
  * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
package kafka.api

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.clients.telemetry.TelemetryState
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.utils.Utils
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

import java.time.Duration
import java.util.Properties

class TelemetryMetricsTest extends IntegrationTestHarness {

  val topicName = "telemetry-integration-test"

  override def brokerCount: Int = 3

  @Test
  def testBasic(): Unit = {
    val producer = createProducer()
    val record = new ProducerRecord(topicName, "key".getBytes, "value".getBytes)
    producer.send(record)

    val clientInstanceId = producer.clientInstanceId(Duration.ofSeconds(1))
    assertNotNull(clientInstanceId)

    val tmi = producer.asInstanceOf[KafkaProducer[ByteArraySerializer, ByteArraySerializer]].tmi
    assertNotNull(tmi)

    val subscription = tmi.subscription
    assertEquals(subscription.clientInstanceId.toString, clientInstanceId)

    Utils.sleep(10000)

    assertEquals(TelemetryState.push_needed, tmi.state)
  }

  @Test
  def testDisableMetricsPush(): Unit = {
    val properties = new Properties()
    properties.put(ProducerConfig.ENABLE_METRICS_PUSH_CONFIG, false)
    val producer = createProducer(configOverrides = properties)
    assertNull(producer.asInstanceOf[KafkaProducer[ByteArraySerializer, ByteArraySerializer]].tmi())
    val record = new ProducerRecord(topicName, "key".getBytes, "value".getBytes)
    producer.send(record)

    val clientInstanceId = producer.clientInstanceId(Duration.ofSeconds(1))
    assertNull(clientInstanceId)
  }

}
