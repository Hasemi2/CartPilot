package com.cartpilot.common.security;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class FixedCurrentUserProvider implements CurrentUserProvider {

	private static final UUID FIXED_USER_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

	@Override
	public UUID currentUserId() {
		return FIXED_USER_ID;
	}
}
