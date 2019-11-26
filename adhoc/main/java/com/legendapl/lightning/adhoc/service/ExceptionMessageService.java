package com.legendapl.lightning.adhoc.service;

import javax.ws.rs.ProcessingException;

import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AccessDeniedException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.AuthenticationFailedException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.BadRequestException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.InternalServerErrorException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ModificationNotAllowedException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.NoResultException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ResourceInUseException;
import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ResourceNotFoundException;
import com.legendapl.lightning.adhoc.common.AdhocUtils;

public class ExceptionMessageService {

	public static String getMessage(Exception e) {
		
		if (e instanceof ProcessingException)
			return AdhocUtils.getString("SERVER_ERROR_Processing");
		
		else if (e instanceof AccessDeniedException)
			return AdhocUtils.getString("SERVER_ERROR_AccessDenied");
		
		else if (e instanceof ResourceNotFoundException)
			return AdhocUtils.getString("SERVER_ERROR_ResourceNotFound");
		
		else if (e instanceof ResourceInUseException)
			return AdhocUtils.getString("SERVER_ERROR_ResourceInUse");
		
		else if (e instanceof AuthenticationFailedException)
			return AdhocUtils.getString("SERVER_ERROR_AuthenticationFailed");
		
		else if (e instanceof InternalServerErrorException)
			return AdhocUtils.getString("SERVER_ERROR_InternalServerError");
		
		else if (e instanceof ModificationNotAllowedException)
			return AdhocUtils.getString("SERVER_ERROR_ModificationNotAllowed");
		
		else if (e instanceof NoResultException)
			return AdhocUtils.getString("SERVER_ERROR_NoResult");
		
		else if (e instanceof BadRequestException)
			return AdhocUtils.getString("SERVER_ERROR_BadRequest");
		
		else
			return AdhocUtils.getString("SERVER_ERROR_Unknown");
		
	}
	
}
