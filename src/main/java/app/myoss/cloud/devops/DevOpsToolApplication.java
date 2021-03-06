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

package app.myoss.cloud.devops;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import app.myoss.cloud.web.spring.boot.BootApplication;
import lombok.extern.slf4j.Slf4j;

/**
 * 项目启动类
 *
 * @author Jerry.Chen
 * @since 2020年3月25日 下午4:21:10
 */
@Slf4j
@SpringBootApplication
public class DevOpsToolApplication {
    /**
     * 项目启动类
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        BootApplication.run(log, false, DevOpsToolApplication.class, args);
    }
}
