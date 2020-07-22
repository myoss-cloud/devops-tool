/*
 * Copyright 2018-2020 https://github.com/myoss
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
 *
 */

package app.myoss.cloud.devops.web;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriTemplateHandler;

import app.myoss.cloud.core.lang.json.JsonApi;
import app.myoss.cloud.core.lang.json.JsonArray;
import app.myoss.cloud.core.lang.json.JsonObject;
import app.myoss.cloud.web.utils.RestClient;
import lombok.extern.slf4j.Slf4j;

/**
 * Sonatype Nexus3 管理工具API
 *
 * @author Jerry.Chen
 * @since 2020年4月15日 上午9:47:31
 */
@Slf4j
@RequestMapping("/nexus3")
@RestController
public class SonatypeNexus3Controller {

    /**
     * 删除 nexus3 某个仓库的所有文件，适用于第一次快速部署 nexus3 服务之后使用备份文件进行还原仓库，但又需要清空掉原有的仓库索引文件
     *
     * <pre>
     * postman 请求地址: http://localhost:8080/nexus3/delete/components/folder
     * 请求参数如下:
     {
     "hostName": "nexus3域名地址",
     "repositoryId": "maven-central",
     "userName": "账户",
     "password": "密码"
     }
     * </pre>
     *
     * @param params 请求参数
     * @return 处理结果
     */
    @RequestMapping("/delete/components/folder")
    public String deleteComponentsFolder(@RequestBody Map<String, String> params) {
        // curl -X GET -u 'username:password' "http://repository.example.com/service/rest/v1/components?repository={repositoryId}" -H "accept: application/json"
        // curl -X DELETE -u 'username:password' "http://repository.example.com/service/rest/v1/components/{id}" -H "accept: application/json"

        String hostName = params.get("hostName");
        String repositoryId = params.get("repositoryId");
        String userName = params.get("userName");
        String password = params.get("password");
        String continuationToken = params.get("continuationToken");

        deleteComponentsFolder(hostName, repositoryId, userName, password, continuationToken);
        deleteAssets(hostName, repositoryId, userName, password, continuationToken);
        return "ok";
    }

    private void deleteComponentsFolder(String hostName, String repositoryId, String userName, String password,
                                        String continuationToken) {
        String getUrl = hostName + "/service/rest/v1/components?repository={0}";
        if (StringUtils.isNotBlank(continuationToken)) {
            getUrl += "&continuationToken={1}";
        }
        String deleteUrl = hostName + "/service/rest/v1/components/{0}";
        continuationToken = queryAndDelete(repositoryId, userName, password, continuationToken, getUrl, deleteUrl);

        if (StringUtils.isNotBlank(continuationToken)) {
            // 循环删除
            deleteComponentsFolder(hostName, repositoryId, userName, password, continuationToken);
        }
    }

    private void deleteAssets(String hostName, String repositoryId, String userName, String password,
                              String continuationToken) {
        String getUrl = hostName + "/service/rest/v1/assets?repository={0}";
        if (StringUtils.isNotBlank(continuationToken)) {
            getUrl += "&continuationToken={1}";
        }
        String deleteUrl = hostName + "/service/rest/v1/assets/{0}";
        continuationToken = queryAndDelete(repositoryId, userName, password, continuationToken, getUrl, deleteUrl);

        if (StringUtils.isNotBlank(continuationToken)) {
            // 循环删除
            deleteAssets(hostName, repositoryId, userName, password, continuationToken);
        }
    }

    private String queryAndDelete(String repositoryId, String userName, String password, String continuationToken,
                                  String getUrl, String deleteUrl) {
        HttpHeaders httpHeaders = getHttpHeaders(userName, password);
        String queryResult = RestClient.getForString(httpHeaders, getUrl, repositoryId, continuationToken);
        JsonObject jsonObject = JsonApi.fromJson(queryResult);
        continuationToken = jsonObject.getAsString("continuationToken");
        JsonArray items = jsonObject.getAsJsonArray("items");
        UriTemplateHandler uriTemplateHandler = RestClient.getRestTemplate().getUriTemplateHandler();
        for (Object item : items) {
            JsonObject asset = (JsonObject) item;
            String id = asset.getAsString("id");
            URI uri = uriTemplateHandler.expand(deleteUrl, id);
            String deleteResult = RestClient.exchange(httpHeaders, MediaType.APPLICATION_JSON, uri, HttpMethod.DELETE,
                    null, String.class);
            log.info(deleteResult);
        }
        return continuationToken;
    }

    private HttpHeaders getHttpHeaders(String userName, String password) {
        String auth = userName + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", authHeader);
        return httpHeaders;
    }
}
