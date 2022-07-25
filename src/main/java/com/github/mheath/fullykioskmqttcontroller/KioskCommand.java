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

/**
 * Full list of Fully Kiosk commands can be found at https://www.fully-kiosk.com/en/#rest.
 */
public enum KioskCommand {

	// Device Info and Basic Features
	GET_DEVICE_INFO("getDeviceInfo"),

	CLEAR_CACHE("clearCache"),
	CLEAR_COOKIES("clearCookies"),
	CLEAR_WEBSTORAGE("clearWebStorage"),
	CLOSE_TAB("closeTab"),
	FOCUS_TAB("focusTab"),
	FORCE_SLEEP("forceSleep"),
	LOAD_START_URL("loadStartUrl"),
	LOAD_URL("loadUrl"),
	REFRESH_TAB("refreshTab"),
	SCREEN_ON("screenOn"),
	SCREEN_OFF("screenOff"),
	TRIGGER_MOTION("triggerMotion");

	private final String commandString;

	KioskCommand(String commandString) {
		this.commandString = commandString;
	}

	public String getCommandString() {
		return commandString;
	}

}
