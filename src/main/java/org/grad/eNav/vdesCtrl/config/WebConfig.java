/*
 * Copyright (c) 2021 GLA Research and Development Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grad.eNav.vdesCtrl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * The WebConfig Class
 *
 * This is the main configuration class for the Web MVC operations.
 *
 * @author Nikolaos Vastardis (email: Nikolaos.Vastardis@gla-rad.org)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * As of Spring Framework 6.0, the trailing slash matching configuration
     * option has been deprecated and its default value set to false. This
     * means that previously, the following controller would match both
     * "GET /some/greeting" and "GET /some/greeting/". To disable this
     * functionality and mirror the previous version behaviour we need to
     * do this. Note that this functionality has been deprecated so we need
     * to be careful.
     *
     * @param pathMatchConfigurer   the path match configurer
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer pathMatchConfigurer) {
        pathMatchConfigurer.setUseTrailingSlashMatch(true);
    }

    /**
     * Add the static resources and webjars to the web resources.
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**",
                    "/css/**",
                    "/images/**",
                    "/lib/**",
                    "/src/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/",
                    "classpath:/static/css/",
                    "classpath:/static/images/",
                    "classpath:/static/lib/",
                    "classpath:/static/src/")
                .resourceChain(false);
        registry.setOrder(1);
    }

    /**
     * Make the index.html our main page so that it's being picked up by
     * Thymeleaf.
     *
     * @param registry The View Controller Registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index");
    }

}
