/**
 * Copyright (C) 2013 Salzburg Research.
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
package kiwi.core.model.content;

import at.newmedialab.sesame.commons.model.Namespaces;
import at.newmedialab.sesame.facading.annotations.RDF;
import at.newmedialab.sesame.facading.model.Facade;

/**
 * User: Thomas Kurz
 * Date: 25.01.11
 * Time: 10:03
 */
public interface MediaContentItem  extends Facade {


    /**
     * Return the file system path of this content.
     * @return
     */
    @RDF(Namespaces.NS_KIWI_CORE+"hasContentPath")
    public String getContentPath();

    /**
     * Set the file system path of this content.
     * @param path
     */
    public void setContentPath(String path);

    /**
     * Return the URI location of the content for this resource
     * @return
     */
    @RDF(Namespaces.NS_KIWI_CORE+"hasContentLocation")
    public String getContentLocation();

    /**
     * Set the URI location of the content for this resource
     * @return
     */
    public void setContentLocation(String location);
}
