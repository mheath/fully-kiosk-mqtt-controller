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

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "mqtt")
record MqttClientProperties(String serverUri, String clientId, String userName, String password) {}

@Configuration
@EnableConfigurationProperties(MqttClientProperties.class)
public class MqttClientConfiguration {

	@Bean
	IMqttClient mqttClient(MqttClientProperties properties) throws MqttException {
		var mqttClient = new MqttClient(properties.serverUri(), properties.clientId(), new MemoryPersistence());
		var options = new MqttConnectOptions();
		options.setUserName(properties.userName());
		options.setPassword(properties.password().toCharArray());
		options.setAutomaticReconnect(true);
		mqttClient.connect(options);
		return mqttClient;
	}

}
