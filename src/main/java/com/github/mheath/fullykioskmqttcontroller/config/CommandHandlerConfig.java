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

import javax.annotation.PostConstruct;

import com.github.mheath.fullykioskmqttcontroller.CommandHandler;
import com.github.mheath.fullykioskmqttcontroller.FullyKioskClient;
import com.github.mheath.fullykioskmqttcontroller.FullyKioskDiscovery;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandHandlerConfig {

	private static final Logger log = LoggerFactory.getLogger(CommandHandlerConfig.class);

	private final KioskProperties kioskProperties;
	private final IMqttClient mqttClient;
	private final FullyKioskDiscovery fullyKioskDiscovery;
	private final FullyKioskClient fullyKioskClient;

	@Autowired
	public CommandHandlerConfig(KioskProperties kioskProperties, IMqttClient mqttClient, FullyKioskDiscovery fullyKioskDiscovery, FullyKioskClient fullyKioskClient) {
		this.kioskProperties = kioskProperties;
		this.mqttClient = mqttClient;
		this.fullyKioskDiscovery = fullyKioskDiscovery;
		this.fullyKioskClient = fullyKioskClient;
	}

	@PostConstruct
	void processCommandHandlers() {
		this.kioskProperties.controlTopics().forEach(this::initializeCommandHandler);
	}

	private void initializeCommandHandler(String name, TopicProperties topicProperties) {
		try {
			log.info("Subscribing to {}", topicProperties.topic());
			this.mqttClient.subscribe(topicProperties.topic(), new CommandHandler(
					this.mqttClient,
					this.fullyKioskDiscovery,
					this.fullyKioskClient,
					topicProperties.commands()));
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}

}
