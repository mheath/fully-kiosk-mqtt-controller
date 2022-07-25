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

package com.github.mheath.fullykioskmqttcontroller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.github.mheath.fullykioskmqttcontroller.config.KioskCommandProperties;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandHandler implements IMqttMessageListener {

	private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

	private final IMqttClient mqttClient;
	private final FullyKioskDiscovery kioskDiscovery;
	private final FullyKioskClient kioskClient;
	private final List<KioskCommandProperties> kioskCommandProperties;

	public CommandHandler(
			IMqttClient mqttClient,
			FullyKioskDiscovery kioskDiscovery,
			FullyKioskClient kioskClient,
			List<KioskCommandProperties> kioskCommandProperties) {
		this.mqttClient = mqttClient;
		this.kioskDiscovery = kioskDiscovery;
		this.kioskClient = kioskClient;
		this.kioskCommandProperties = kioskCommandProperties;
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) {
		log.debug("Received {} {}", topic, message);

		kioskCommandProperties.forEach(commandProperties -> {
			try {
				CompletableFuture.allOf(kioskDiscovery.getKioskStream().map(kiosk ->
						this.sendCommand(commandProperties.command(), kiosk.ip(), commandProperties.params())
								.thenAccept(reply -> log.debug("Reply from {} ({}): {}",
										kiosk.deviceName(), kiosk.ip(), reply)))
								.toList().toArray(new CompletableFuture[0]))
						.thenAccept(v -> log.debug("Command(s) sent: {}", commandProperties))
						.get();
			} catch (Exception e) {
				log.error("Error sending command {}", commandProperties, e);
			}
		});
	}

	private CompletableFuture<Map<String, Object>> sendCommand(KioskCommand kioskCommand, String ip, Map<String, Object> staticParams) {
		final Map<String, Object> params = staticParams == null ? Collections.emptyMap() : staticParams;
		log.debug("Sending command {} with params {} to {}", kioskCommand, params, ip);
		try {
			return this.kioskClient.sendCommandAsync(kioskCommand, ip, params);
		} catch (Exception e) {
			log.error("Error sending command {} with params {} to {}", kioskCommand, params, ip, e);
			return CompletableFuture.failedFuture(e);
		}
	}
}
