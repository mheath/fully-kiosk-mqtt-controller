/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mheath.fullykioskmqttcontroller.config;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mheath.fullykioskmqttcontroller.CommandHandler;
import com.github.mheath.fullykioskmqttcontroller.FullyKioskClient;
import com.github.mheath.fullykioskmqttcontroller.FullyKioskDiscovery;
import com.github.mheath.fullykioskmqttcontroller.KioskCommand;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "kiosk")
record KioskProperties(String adminPassword, String deviceInfoTopic, Map<String, TopicProperties> controlTopics) {}

record TopicProperties(String topic, List<KioskCommandProperties> commands) {}

@Configuration
@EnableConfigurationProperties(KioskProperties.class)
public class KioskControlConfig {

	public static final String DEFAULT_FULLY_DEVICE_INFO_TOPIC = "fully/deviceInfo/+";

	private final IMqttClient mqttClient;
	private final ObjectMapper mapper;

	private final KioskProperties kioskProperties;

	@Autowired
	public KioskControlConfig(IMqttClient mqttClient, ObjectMapper mapper, KioskProperties kioskProperties) {
		this.mqttClient = mqttClient;
		this.mapper = mapper;
		this.kioskProperties = kioskProperties;
	}

	@Bean
	FullyKioskClient fullyKioskClient() {
		return new FullyKioskClient(this.kioskProperties.adminPassword());
	}

	@Bean
	FullyKioskDiscovery fullyKioskDiscovery() throws MqttException {
		var topic = (this.kioskProperties.deviceInfoTopic() != null) ?
				this.kioskProperties.deviceInfoTopic() : DEFAULT_FULLY_DEVICE_INFO_TOPIC;
		return new FullyKioskDiscovery(mqttClient, topic, mapper);
	}

}
