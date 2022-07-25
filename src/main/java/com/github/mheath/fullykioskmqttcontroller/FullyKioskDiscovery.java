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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

public class FullyKioskDiscovery {

	public static final Duration KIOSK_TIMEOUT = Duration.of(1, ChronoUnit.MINUTES);

	private static final Logger log = LoggerFactory.getLogger(FullyKioskDiscovery.class);

	public record DeviceInfo(String deviceId, String ip4, String deviceName) {};

	public record KioskInfo(String ip, String deviceName, Instant lastSeen) {
		KioskInfo(String ip, String deviceName) {
			this(ip, deviceName, Instant.now());
		}
	}

	// Access must be synchronized on #instances
	private final Map<String, KioskInfo> kiosks = new HashMap<>();
	private final ObjectMapper mapper;

	@Autowired
	public FullyKioskDiscovery(IMqttClient mqttClient, String deviceInfoTopic, ObjectMapper mapper)
			throws MqttException {
		this.mapper = mapper;
		mqttClient.subscribe(deviceInfoTopic, this::deviceInfoListener);
		log.info("Discovering Full Kiosk instances on topic {}", deviceInfoTopic);
	}

	public Stream<KioskInfo> getKioskStream() {
		synchronized (this.kiosks) {
			var values = kiosks.values();
			values.removeIf(this::kioskTimedOut);
			return new ArrayList<>(values).stream();
		}
	}

	private boolean kioskTimedOut(KioskInfo kioskInfo) {
		final boolean remove = kioskInfo.lastSeen().plus(KIOSK_TIMEOUT).isBefore(Instant.now());
		if (remove) {
			log.info("Kiosk timed out {}", kioskInfo);
		}
		return remove;
	}

	void registerKiosk(DeviceInfo info) {
		synchronized (kiosks) {
			final KioskInfo kioskInfo = new KioskInfo(info.ip4(), info.deviceName());
			if (!kiosks.containsKey(info.deviceId())) {
				log.info("Discovered new kiosk {}", info);
			}
			kiosks.put(info.deviceId(), kioskInfo);
		}
	}

	private void deviceInfoListener(String topic, MqttMessage message) {
		try {
			var deviceInfo = mapper.readValue(message.getPayload(), DeviceInfo.class);
			log.debug("Received {} {}", topic, deviceInfo);
			registerKiosk(deviceInfo);
		} catch (IOException e) {
			log.error("Error handling deviceInfo message", e);
			throw new RuntimeException(e);
		}
	}

}
