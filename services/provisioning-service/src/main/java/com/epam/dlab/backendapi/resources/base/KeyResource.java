/*
 * Copyright (c) 2017, EPAM SYSTEMS INC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.epam.dlab.backendapi.resources.base;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.commands.*;
import com.epam.dlab.backendapi.core.response.folderlistener.FolderListenerExecutor;
import com.epam.dlab.dto.base.keyload.ReuploadFile;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.KeyAPI;
import com.epam.dlab.utils.FileUtils;
import com.epam.dlab.utils.UsernameUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import io.dropwizard.auth.Auth;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Provides API for reuploading keys
 */
@Path(KeyAPI.REUPLOAD_KEY)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
public class KeyResource implements DockerCommands {

	@Inject
	protected RESTService selfService;
	@Inject
	private ProvisioningServiceApplicationConfiguration configuration;
	@Inject
	private FolderListenerExecutor folderListenerExecutor;
	@Inject
	private ICommandExecutor commandExecutor;
	@Inject
	private CommandBuilder commandBuilder;

	@POST
	public String reuploadKey(@Auth UserInfo ui, ReuploadFile dto) throws IOException {
		String edgeUserName = dto.getEdgeUserName();
		String filename = UsernameUtils.replaceWhitespaces(edgeUserName) + KeyAPI.KEY_EXTENTION;
		FileUtils.deleteFile(filename, configuration.getKeyDirectory());
		FileUtils.saveToFile(filename, configuration.getKeyDirectory(), dto.getContent());
		return reuploadKeyAction(edgeUserName, ui.getName(), dto.getRunningEnvironment(), DockerAction.REUPLOAD_KEY);
	}

	//TODO refactor Docker command corresponding to DevOps' requirement
	private String reuploadKeyAction(String edgeUserName, String userName, Map<String, List<String>>
			runningEnvironment,
									 DockerAction action) throws JsonProcessingException {
		log.debug("{} for edge user {}", action, edgeUserName);
		String uuid = DockerCommands.generateUUID();

		RunDockerCommand runDockerCommand = new RunDockerCommand()
				.withInteractive()
				.withName(nameContainer(edgeUserName, action.toString()))
				.withVolumeForRootKeys(configuration.getKeyDirectory())
				.withVolumeForResponse(configuration.getKeyLoaderDirectory())
				.withVolumeForLog(configuration.getDockerLogDirectory(), getResourceType())
				.withResource(getResourceType())
				.withRequestId(uuid)
				.withConfKeyName(configuration.getAdminKey())
				.withImage(configuration.getEdgeImage())
				.withAction(action);

		String command = commandBuilder.buildCommand(runDockerCommand, null);
		log.trace("Docker command:  {}", command);
		commandExecutor.executeAsync(userName, uuid, command);
		return uuid;
	}

	@Override
	public String getResourceType() {
		return "RES_TYPE";
	}
}
