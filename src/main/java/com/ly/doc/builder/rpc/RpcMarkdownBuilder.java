/*
 * Copyright (C) 2018-2023 smart-doc
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ly.doc.builder.rpc;

import java.util.List;

import com.ly.doc.constants.DocGlobalConstants;
import com.ly.doc.model.ApiConfig;
import com.ly.doc.model.rpc.RpcApiDoc;
import com.power.common.util.DateTimeUtil;
import com.ly.doc.helper.JavaProjectBuilderHelper;
import com.thoughtworks.qdox.JavaProjectBuilder;

/**
 * @author yu 2020/5/16.
 */
public class RpcMarkdownBuilder {

    private static final String API_EXTENSION = "Api.md";

    private static final String DATE_FORMAT = "yyyyMMddHHmm";

    /**
     * @param config ApiConfig
     */
    public static void buildApiDoc(ApiConfig config) {
        JavaProjectBuilder javaProjectBuilder = JavaProjectBuilderHelper.create();
        buildApiDoc(config, javaProjectBuilder);
    }

    /**
     * Only for smart-doc maven plugin and gradle plugin.
     *
     * @param apiConfig          ApiConfig
     * @param javaProjectBuilder ProjectDocConfigBuilder
     */
    public static void buildApiDoc(ApiConfig apiConfig, JavaProjectBuilder javaProjectBuilder) {
        apiConfig.setAdoc(Boolean.FALSE);
        RpcDocBuilderTemplate builderTemplate = new RpcDocBuilderTemplate();
        builderTemplate.checkAndInit(apiConfig,Boolean.TRUE);
        List<RpcApiDoc> apiDocList = builderTemplate.getRpcApiDoc(apiConfig, javaProjectBuilder);
        if (apiConfig.isAllInOne()) {
            String version = apiConfig.isCoverOld() ? "" : "-V" + DateTimeUtil.long2Str(System.currentTimeMillis(), DATE_FORMAT);
            String docName = builderTemplate.allInOneDocName(apiConfig, "rpc-all" + version, ".md");
            builderTemplate.buildAllInOne(apiDocList, apiConfig, javaProjectBuilder, DocGlobalConstants.RPC_ALL_IN_ONE_MD_TPL, docName);
        } else {
            builderTemplate.buildApiDoc(apiDocList, apiConfig, DocGlobalConstants.RPC_API_DOC_MD_TPL, API_EXTENSION);
            builderTemplate.buildErrorCodeDoc(apiConfig, DocGlobalConstants.ERROR_CODE_LIST_MD_TPL, DocGlobalConstants.ERROR_CODE_LIST_MD, javaProjectBuilder);
        }
    }
}
