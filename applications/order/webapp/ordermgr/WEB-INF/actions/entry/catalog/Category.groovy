/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * This script is also referenced by the [Scipio shop]'s screens and
 * should not contain order component's specific code.
 */

import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.product.catalog.*;
import org.ofbiz.product.category.CategoryWorker;
import org.ofbiz.product.category.CategoryContentWrapper;
import org.ofbiz.product.store.ProductStoreWorker;

detailScreen = "categorydetail";
catalogName = CatalogWorker.getCatalogName(request);

productCategoryId = request.getAttribute("productCategoryId") ?: parameters.category_id;
context.productCategoryId = productCategoryId;

context.productStore = ProductStoreWorker.getProductStore(request);

pageTitle = null;
metaDescription = null;
metaKeywords = null;

/* NOTE DEJ20070220: this is a weird way to do this and caused unacceptable side effects as described in the related
 * comment in the Main.groovy file
 *
 * NOTE JLR 20070221 this should be done using the same method than in add to cart. I will do it like that and remove all this after.
 *
if (productCategoryId) {
    session.setAttribute("productCategoryId", productCategoryId);// for language change
    previousParams = session.getAttribute("_PREVIOUS_PARAMS_");
    if (previousParams) {
        previousParams = UtilHttp.stripNamedParamsFromQueryString(previousParams, ["category_id"]);
        previousParams += "&category_id=" + productCategoryId;
    } else {
        previousParams = "category_id=" + productCategoryId;
    }
    session.setAttribute("_PREVIOUS_PARAMS_", previousParams);    // for login
    context.previousParams = previousParams;
}
 */

category = from("ProductCategory").where("productCategoryId", productCategoryId).cache(true).queryOne();
if (category) {
    if (category.detailScreen) {
        detailScreen = category.detailScreen;
    }
    categoryPageTitle = from("ProductCategoryContentAndInfo").where("productCategoryId", productCategoryId, "prodCatContentTypeId", "PAGE_TITLE").cache(true).queryList();
    if (categoryPageTitle) {
        pageTitle = from("ElectronicText").where("dataResourceId", categoryPageTitle.get(0).dataResourceId).cache(true).queryOne();
    }
    categoryMetaDescription = from("ProductCategoryContentAndInfo").where("productCategoryId", productCategoryId, "prodCatContentTypeId", "META_DESCRIPTION").cache(true).queryList();
    if (categoryMetaDescription) {
        metaDescription = from("ElectronicText").where("dataResourceId", categoryMetaDescription.get(0).dataResourceId).cache(true).queryOne();
    }
    categoryMetaKeywords = from("ProductCategoryContentAndInfo").where("productCategoryId", productCategoryId, "prodCatContentTypeId", "META_KEYWORD").cache(true).queryList();
    if (categoryMetaKeywords) {
        metaKeywords = from("ElectronicText").where("dataResourceId", categoryMetaKeywords.get(0).dataResourceId).cache(true).queryOne();
    }
    categoryContentWrapper = new CategoryContentWrapper(category, request);
    
    // Scipio: Do NOT HTML-escape this here
    categoryDescription = categoryContentWrapper.get("DESCRIPTION", "raw").toString();

    // Scipio: don't want page title overridden/forced by groovy
    if (pageTitle) {
        //context.title = pageTitle.textData;
        context.categoryTitle = pageTitle.textData;
    } else {
        // Scipio: Do NOT HTML-escape this here
        //context.title = categoryContentWrapper.get("CATEGORY_NAME", "raw").toString();
        context.categoryTitle = categoryContentWrapper.get("CATEGORY_NAME", "raw".toString());
    }

    if (metaDescription) {
        context.metaDescription = metaDescription.textData;
    } else {
        if (categoryDescription) {
            context.metaDescription = categoryDescription;
        }
    }

    if (metaKeywords) {
        context.metaKeywords = metaKeywords.textData;
    } else {
        if (categoryDescription) {
            context.metaKeywords = categoryDescription + ", " + catalogName;
        } else {
            context.metaKeywords = catalogName;
        }
    }
    context.productCategory = category;
}

// check the catalogs template path and update
templatePathPrefix = CatalogWorker.getTemplatePathPrefix(request);
if (templatePathPrefix) {
    detailScreen = templatePathPrefix + detailScreen;
}
context.detailScreen = detailScreen;

request.setAttribute("productCategoryId", productCategoryId);
request.setAttribute("defaultViewSize", 10);
request.setAttribute("limitView", true);
