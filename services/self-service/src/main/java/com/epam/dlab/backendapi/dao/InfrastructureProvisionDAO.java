/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.dao;

import com.epam.dlab.backendapi.api.instance.UserComputationalResourceDTO;
import com.epam.dlab.backendapi.api.instance.UserInstanceDTO;
import com.epam.dlab.dto.StatusBaseDTO;
import com.epam.dlab.dto.computational.ComputationalStatusDTO;
import com.epam.dlab.dto.exploratory.ExploratoryStatusDTO;
import com.epam.dlab.exceptions.DlabException;
import com.mongodb.MongoWriteException;
import org.bson.Document;

import java.util.Optional;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class InfrastructureProvisionDAO extends BaseDAO {
    public static final String USER_EXPLORATORY_NAME = "user_exploratory_name";
    private static final String EXPLORATORY_NAME = "exploratory_name";
    public static final String COMPUTATIONAL_RESOURCES = "computational_resources";
    public static final String USER_COMPUTATIONAL_NAME = "user_computational_name";
    public static final String COMPUTATIONAL_NAME = "computational_name";

    private static final String SET = "$set";

    public Iterable<Document> find(String user) {
        return mongoService.getCollection(USER_INSTANCES).find(eq(USER, user));
    }

    public Iterable<Document> findShapes() {
        return mongoService.getCollection(SHAPES).find();
    }

    public String fetchExploratoryName(String user, String exploratoryName) {
        return mongoService.getCollection(USER_INSTANCES)
                .find(and(eq(USER, user), eq(USER_EXPLORATORY_NAME, exploratoryName))).first()
                .getOrDefault(EXPLORATORY_NAME, EMPTY).toString();
    }

    public boolean insertExploratory(UserInstanceDTO dto) {
        try {
            insertOne(USER_INSTANCES, dto);
            return true;
        } catch (MongoWriteException e) {
            return false;
        }
    }

    public void updateExploratoryStatus(StatusBaseDTO dto) {
        update(USER_INSTANCES, and(eq(USER, dto.getUser()), eq(USER_EXPLORATORY_NAME, dto.getUserExploratoryName())), set(STATUS, dto.getStatus()));
    }

    public void updateExploratoryStatusAndName(ExploratoryStatusDTO dto) {
        Document updates = new Document(SET, new Document(STATUS, dto.getStatus()).append(EXPLORATORY_NAME, dto.getExploratoryName()));
        update(USER_INSTANCES, and(eq(USER, dto.getUser()), eq(USER_EXPLORATORY_NAME, dto.getUserExploratoryName())), updates);
    }

    public void updateComputationalStatusesForExploratory(StatusBaseDTO dto) {
        find(USER_INSTANCES, and(eq(USER, dto.getUser()), eq(USER_EXPLORATORY_NAME, dto.getUserExploratoryName())), UserInstanceDTO.class)
                .ifPresent(instance -> instance.getResources().forEach(resource -> {
                    updateComputationalStatus(dto, resource.getUserComputationalName());
                }));
    }

    public boolean addComputational(String user, String name, UserComputationalResourceDTO computationalDTO) {
        Optional<UserInstanceDTO> optional = find(USER_INSTANCES, and(eq(USER, user), eq(USER_EXPLORATORY_NAME, name)), UserInstanceDTO.class);
        if (optional.isPresent()) {
            UserInstanceDTO dto = optional.get();
            long count = dto.getResources().stream()
                    .filter(i -> computationalDTO.getUserComputationalName().equals(i.getUserComputationalName()))
                    .count();
            if (count == 0) {
                update(USER_INSTANCES, eq(ID, dto.getId()), push(COMPUTATIONAL_RESOURCES, convertToBson(computationalDTO)));
                return true;
            } else {
                return false;
            }
        } else {
            throw new DlabException("User '" + user + "' has no records for environment '" + name + "'");
        }
    }

    public String fetchComputationalName(String user, String userExploratoryName, String userComputationalName) {
        return mongoService.getCollection(USER_INSTANCES)
                .find(and(eq(USER, user), eq(USER_EXPLORATORY_NAME, userExploratoryName),
                        eq(COMPUTATIONAL_RESOURCES + FIELD_DELIMETER + USER_COMPUTATIONAL_NAME, userComputationalName))).first()
                .getOrDefault(COMPUTATIONAL_NAME, EMPTY).toString();
    }

    public void updateComputationalStatus(ComputationalStatusDTO dto) {
        updateComputationalStatus(dto.getUser(), dto.getUserExploratoryName(), dto.getComputationalName(), dto.getStatus());
    }

    private void updateComputationalStatus(StatusBaseDTO dto, String computationalName) {
        updateComputationalStatus(dto.getUser(), dto.getUserExploratoryName(), computationalName, dto.getStatus());
    }

    private void updateComputationalStatus(String user, String exploratoryName, String computationalName, String status) {
        try {
            update(USER_INSTANCES, and(eq(USER, user), eq(USER_EXPLORATORY_NAME, exploratoryName)
                    , eq(COMPUTATIONAL_RESOURCES + FIELD_DELIMETER + USER_COMPUTATIONAL_NAME, computationalName)),
                    set(COMPUTATIONAL_RESOURCES + FIELD_SET_DELIMETER + STATUS, status));
        } catch (Throwable t) {
            throw new DlabException("Could not update computational resource status", t);
        }
    }

    public void updateComputationalStatusAndName(ComputationalStatusDTO dto) {
        try {
            Document updates = new Document(SET, new Document(COMPUTATIONAL_RESOURCES + FIELD_SET_DELIMETER + STATUS, dto.getStatus())
                    .append(COMPUTATIONAL_RESOURCES + FIELD_SET_DELIMETER + COMPUTATIONAL_NAME, dto.getComputationalName()));
            update(USER_INSTANCES, and(eq(USER, dto.getUser()), eq(USER_EXPLORATORY_NAME, dto.getUserExploratoryName())
                    , eq(COMPUTATIONAL_RESOURCES + FIELD_DELIMETER + USER_COMPUTATIONAL_NAME, dto.getUserComputationalName())),
                    updates);
        } catch (Throwable t) {
            throw new DlabException("Could not update computational resource status", t);
        }
    }
}
