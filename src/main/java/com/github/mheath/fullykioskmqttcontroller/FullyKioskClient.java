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
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.util.UriComponentsBuilder;

public class FullyKioskClient {

	private static final Logger log = LoggerFactory.getLogger(FullyKioskClient.class);

	private final HttpClient httpClient;
	private final String adminPassword;
	private final ObjectMapper mapper = new ObjectMapper();

	public FullyKioskClient(String adminPassword) {
		this.adminPassword = adminPassword;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.of(1, ChronoUnit.SECONDS))
				.build();
	}

	private static class MapTypeReference extends TypeReference<Map<String, Object>> {
		static final MapTypeReference ref = new MapTypeReference();
	}

	public CompletableFuture<Map<String, Object>> sendCommandAsync(KioskCommand command, String ip, Map<String, Object> params) {
		var uriBuilder = UriComponentsBuilder.newInstance()
				.scheme("http")
				.host(ip)
				.port(2323)
				.queryParam("password", this.adminPassword)
				.queryParam("type", "json")
				.queryParam("cmd", command.getCommandString());
		params.forEach(uriBuilder::queryParam);
		var request = HttpRequest.newBuilder(uriBuilder.build().toUri())
				.GET()
				.build();
		return this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
			try {
				var map = mapper.readValue(response.body(), MapTypeReference.ref);
				if (map.get("status").equals("Error")) {
					throw new RuntimeException(map.get("statustext").toString());
				}
				return map;
			} catch (JsonProcessingException e) {
				log.error("Error processing response body", e);
				throw new RuntimeException(e);
			}
		});
	}

}
