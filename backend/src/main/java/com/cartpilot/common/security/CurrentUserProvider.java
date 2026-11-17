package com.cartpilot.common.security;

import java.util.UUID;

public interface CurrentUserProvider {

	UUID currentUserId();
}
