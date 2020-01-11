package com.power.doc.builder;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.power.common.util.FileUtil;
import com.power.doc.constants.DocGlobalConstants;
import com.power.doc.model.ApiConfig;
import com.power.doc.model.ApiDoc;
import com.power.doc.model.ApiMethodDoc;
import com.power.doc.model.ApiReqHeader;
import com.power.doc.model.postman.InfoBean;
import com.power.doc.model.postman.ItemBean;
import com.power.doc.model.postman.RequestItem;
import com.power.doc.model.postman.request.RequestBean;
import com.power.doc.model.postman.request.body.BodyBean;
import com.power.doc.model.postman.request.header.HeaderBean;
import com.power.doc.template.IDocBuildTemplate;
import com.power.doc.template.SpringBootDocBuildTemplate;
import com.thoughtworks.qdox.JavaProjectBuilder;

import java.util.ArrayList;
import java.util.List;


/**
 * @author yu 2019/11/21.
 */
public class PostmanJsonBuilder {


    /**
     * 构建postman json
     *
     * @param config 配置文件
     */
    public static void buildPostmanCollection(ApiConfig config) {
        DocBuilderTemplate builderTemplate = new DocBuilderTemplate();
        builderTemplate.checkAndInit(config);
        JavaProjectBuilder javaProjectBuilder = new JavaProjectBuilder();
        ProjectDocConfigBuilder configBuilder = new ProjectDocConfigBuilder(config, javaProjectBuilder);
        postManCreate(config, configBuilder);

    }

    /**
     * Only for smart-doc-maven-plugin.
     *
     * @param config         ApiConfig Object
     * @param projectBuilder QDOX avaProjectBuilder
     */
    public static void buildPostmanCollection(ApiConfig config, JavaProjectBuilder projectBuilder) {
        DocBuilderTemplate builderTemplate = new DocBuilderTemplate();
        builderTemplate.checkAndInit(config);
        ProjectDocConfigBuilder configBuilder = new ProjectDocConfigBuilder(config, projectBuilder);
        postManCreate(config, configBuilder);
    }

    /**
     * 第一层的Item
     *
     * @param apiDoc
     * @return
     */
    private static ItemBean buildItemBean(ApiDoc apiDoc) {
        ItemBean itemBean = new ItemBean();
        itemBean.setName(apiDoc.getDesc());
        List<ItemBean> itemBeans = new ArrayList<>();
        List<ApiMethodDoc> apiMethodDocs = apiDoc.getList();
        apiMethodDocs.forEach(
                apiMethodDoc -> {
                    ItemBean itemBean1 = buildItem(apiMethodDoc);
                    itemBeans.add(itemBean1);
                }
        );
        itemBean.setItem(itemBeans);
        return itemBean;
    }

    /**
     * 构建第二层的item
     *
     * @param apiMethodDoc
     * @return
     */
    private static ItemBean buildItem(ApiMethodDoc apiMethodDoc) {
        ItemBean item = new ItemBean();
        RequestBean requestBean = new RequestBean();

        item.setName(apiMethodDoc.getDesc());
        item.setDescription(apiMethodDoc.getDetail());

        requestBean.setDescription(apiMethodDoc.getDesc());
        requestBean.setMethod(apiMethodDoc.getType());
        requestBean.setHeader(buildHeaderBeanList(apiMethodDoc));

        requestBean.setBody(buildBodyBean(apiMethodDoc));
        requestBean.setUrl(apiMethodDoc.getRequestExample().getUrl() == null ? apiMethodDoc.getUrl() : apiMethodDoc.getRequestExample().getUrl());

        item.setRequest(requestBean);
        return item;

    }

    /**
     * 构造请求体
     *
     * @param apiMethodDoc
     * @return
     */
    private static BodyBean buildBodyBean(ApiMethodDoc apiMethodDoc) {

        if (apiMethodDoc.getContentType().contains(DocGlobalConstants.JSON_CONTENT_TYPE)) {
            BodyBean bodyBean = new BodyBean(false);
            bodyBean.setMode(DocGlobalConstants.POSTMAN_MODE_RAW);
            if (apiMethodDoc.getRequestExample() != null) {
                bodyBean.setRaw(apiMethodDoc.getRequestExample().getJsonBody());
            }
            return bodyBean;
        } else {
            BodyBean bodyBean = new BodyBean(true);
            bodyBean.setMode(DocGlobalConstants.POSTMAN_MODE_FORMDATA);
            bodyBean.setFormdata(apiMethodDoc.getRequestExample().getFormDataList());
            return bodyBean;
        }

    }

    /**
     * 构造请求头
     *
     * @param apiMethodDoc
     * @return
     */
    private static List<HeaderBean> buildHeaderBeanList(ApiMethodDoc apiMethodDoc) {
        List<HeaderBean> headerBeans = new ArrayList<>();

        List<ApiReqHeader> headers = apiMethodDoc.getRequestHeaders();
        headers.forEach(
                apiReqHeader -> {
                    HeaderBean headerBean = new HeaderBean();
                    headerBean.setKey(apiReqHeader.getName());
                    headerBean.setName(apiReqHeader.getName());
                    headerBean.setDisabled(!apiReqHeader.isRequired());
                    headerBean.setDescription(apiReqHeader.getDesc());
                    headerBeans.add(headerBean);
                }
        );

        return headerBeans;
    }

    private static void postManCreate(ApiConfig config, ProjectDocConfigBuilder configBuilder) {
        IDocBuildTemplate docBuildTemplate = new SpringBootDocBuildTemplate();
        List<ApiDoc> apiDocList = docBuildTemplate.getApiData(configBuilder);
        RequestItem requestItem = new RequestItem();
        requestItem.setInfo(new InfoBean(config.getProjectName()));
        List<ItemBean> itemBeans = new ArrayList<>();
        apiDocList.forEach(
                apiDoc -> {
                    ItemBean itemBean = buildItemBean(apiDoc);
                    itemBeans.add(itemBean);
                }
        );
        requestItem.setItem(itemBeans);
        String filePath = config.getOutPath();
        filePath = filePath + DocGlobalConstants.POSTMAN_JSON;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String data = gson.toJson(requestItem);
        FileUtil.nioWriteFile(data, filePath);
    }

}
