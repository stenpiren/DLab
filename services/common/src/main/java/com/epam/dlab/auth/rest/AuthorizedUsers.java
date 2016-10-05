/*
Copyright 2016 EPAM Systems, Inc.
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.epam.dlab.auth.rest;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jersey.repackaged.com.google.common.collect.Lists;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.UserInfo;

import org.slf4j.Logger;

public class AuthorizedUsers {
	
	private final static Logger LOG = LoggerFactory.getLogger(AuthorizedUsers.class);
	
	private static class UserInfoHolder {
		private final UserInfo userInfo;
		private long expires;
		public UserInfoHolder(UserInfo ui, long timeout) {
			this.userInfo = ui;
			this.expires  = System.currentTimeMillis() + timeout;
		}
		public boolean expired() {
			return expires < System.currentTimeMillis();
		}
		@Override
		public String toString() {
			return "UserInfoHolder [userInfo=" + userInfo + ", expires=" + new Date(expires) + "]";
		}
		
	}
	
	private final static AuthorizedUsers users = new AuthorizedUsers();
	
	private volatile long inactiveTimeout = 600000;
	
	private final Map<String,UserInfoHolder> knownUsers = new ConcurrentHashMap<>();
	
	private AuthorizedUsers() {
		
	}
	
	public static void setInactiveTimeout(long timeout) {
		users.inactiveTimeout = timeout;
	}
	
	public void addUserInfo(String token, UserInfo ui) {
		LOG.debug("Add token: {} {}",token,ui);
		knownUsers.put(token, new UserInfoHolder(ui, inactiveTimeout));
	}

	public void removeUserInfo(String token) {
		knownUsers.remove(token);
	}

	public void cleanup() {
		for(String token: Lists.newArrayList(knownUsers.keySet())) {
			UserInfoHolder uih = knownUsers.get(token);			
			if(uih == null) {
				knownUsers.remove(token);
			} else {
				if(uih.expired()) {
					knownUsers.remove(token);
				}
			}
		}
	}
	
	public UserInfo getUserInfo(String token) {
		if(token == null) {
			return null;
		}
		UserInfoHolder uih = knownUsers.get(token);
		
		if(uih == null) {
			LOG.debug("Unknown token: {}",token);
			return null;
		} else {
			if(uih.expired()) {
				knownUsers.remove(token);
				LOG.debug("Expired token: {} {}",token,uih);
				return null;
			} else {
				uih.expires = System.currentTimeMillis() + inactiveTimeout;
			}
		}
		LOG.debug("Return token: {} {}",token,uih);
		return uih.userInfo;
	}
	
	public static AuthorizedUsers getInstance() {
		return users;
	}
	
}
