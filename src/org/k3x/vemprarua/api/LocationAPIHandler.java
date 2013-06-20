package org.k3x.vemprarua.api;

import java.util.List;

import org.k3x.vemprarua.model.FieldError;
import org.k3x.vemprarua.model.User;

public interface LocationAPIHandler {

	void onCreated(boolean success, User user, List<FieldError> errors);

	void onUpdated(boolean success, User user, List<FieldError> errors);
	
	void onListed(boolean success, int total, List<User> users, List<FieldError> errors);

}
