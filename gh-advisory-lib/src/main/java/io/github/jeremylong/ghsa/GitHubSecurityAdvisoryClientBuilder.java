/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) 2022-2023 Jeremy Long. All Rights Reserved.
 */
package io.github.jeremylong.ghsa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;

/**
 * Used to build an GitHub SecurityAdvisory GraphQL API client. As the GitHubSecurityAdvisoryClient client is
 * autoclosable the builder should be used in a try with resources:
 *
 * <pre>
 * try (GitHubSecurityAdvisoryClient api = GitHubSecurityAdvisoryClientBuilder.aGitHubSecurityAdvisoryClient()
 *         .withApiKey(githubToken).build()) {
 *     while (api.hasNext()) {
 *         Collection&lt;SecurityAdvisory&gt; items = api.next();
 *     }
 * }
 * </pre>
 */
public final class GitHubSecurityAdvisoryClientBuilder {

    /**
     * Reference to the logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GitHubSecurityAdvisoryClientBuilder.class);
    /**
     * The GitHub SecurityAdvisory GraphQL API key.
     */
    private String apiKey;
    /**
     * The endpoint for the GitHub GraphQL API.
     */
    private String endpoint;
    /**
     * The updatedSince filter.
     */
    private ZonedDateTime updatedSince;
    /**
     * The publishedSince filter.
     */
    private ZonedDateTime publishedSince;

    /**
     * Private constructor for a builder.
     */
    private GitHubSecurityAdvisoryClientBuilder() {
    }

    /**
     * Begin building the GitHub GraphQL for SecurityAdvisories Object.
     *
     * @return the builder
     */
    public static GitHubSecurityAdvisoryClientBuilder aGitHubSecurityAdvisoryClient() {
        return new GitHubSecurityAdvisoryClientBuilder();
    }

    /**
     * Use an GitHub SecurityAdvisory GraphQL API key.
     *
     * @param apiKey the GitHub API key.
     * @return the builder
     */
    public GitHubSecurityAdvisoryClientBuilder withApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Use an alternative endpoint for the GitHub SecurityAdvisory GraphQL API.
     *
     * @param endpoint the endpoint for the GitHub SecurityAdvisory GraphQL API
     * @return the builder
     */
    public GitHubSecurityAdvisoryClientBuilder withEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Filter for Security Advisories that have been updated since a specific date/time.
     *
     * @param utcUpdatedSince the UTC date time
     * @return the builder
     */
    public GitHubSecurityAdvisoryClientBuilder withUpdatedSinceFilter(ZonedDateTime utcUpdatedSince) {
        updatedSince = utcUpdatedSince;
        return this;
    }

    /**
     * Filter the results with a range of published since date/time.
     *
     * @param utcStartDate the UTC date time for the range start
     * @return the builder
     */
    public GitHubSecurityAdvisoryClientBuilder withPublishedSinceFilter(ZonedDateTime utcStartDate) {
        publishedSince = utcStartDate;
        return this;
    }

    /**
     * Build the GitHub SecurityAdvisory GraphQL API client.
     *
     * @return the GitHub SecurityAdvisory GraphQL API client
     */
    public GitHubSecurityAdvisoryClient build() {
        GitHubSecurityAdvisoryClient client;
        if (endpoint == null) {
            client = new GitHubSecurityAdvisoryClient(apiKey);
        }  else {
            client = new GitHubSecurityAdvisoryClient(apiKey, endpoint);
        }
        if (publishedSince != null) {
            client.setPublishedSinceFilter(publishedSince);
        }
        if (updatedSince != null) {
            client.setUpdatedSinceFilter(updatedSince);
        }
        return client;
    }
}
