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
 * Copyright (c) 2022-2024 Jeremy Long. All Rights Reserved.
 */
package io.github.jeremylong.vulnz.cli.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public abstract class AbstractNvdCommand extends AbstractJsonCommand {
    /**
     * Reference to the logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractNvdCommand.class);
    @CommandLine.Option(names = {
            "--delay"}, description = "The delay in milliseconds between API calls to the NVD - important if pulling a larger data set without an API Key")
    private int delay;
    @CommandLine.Option(names = {
            "--maxRetry"}, description = "The maximum number of retry attempts on 503 and 429 errors from the NVD API")
    private int maxRetry;
    @CommandLine.Option(names = {
            "--pageCount"}, description = "The number of `pages` of data to retrieve from the NVD if more then a single page is returned")
    private int pageCount = 0;
    @CommandLine.Option(names = {
            "--recordsPerPage"}, description = "The number of records per pages of data to retrieve from the NVD in a single call")
    private int recordsPerPage = 2000;
    @CommandLine.Option(names = {"--threads"}, description = "The number of threads to use when calling the NVd API")
    private int threads = 1;
    // yes - this should not be a string, but seriously down the call path the HttpClient
    // doesn't support passing a header in as a char[]...
    private String apiKey = null;

    protected int getPageCount() {

        return pageCount;
    }

    protected int getThreads() {
        return threads;
    }

    protected int getRecordsPerPage() {

        return recordsPerPage;
    }

    protected int getDelay() {

        return delay;
    }

    protected int getMaxRetry() {
        return maxRetry;
    }

    /**
     * Returns the NVD API Key if supplied.
     *
     * @return the NVD API Key if supplied; otherwise <code>null</code>
     */
    protected String getApiKey() {
        if (apiKey == null && System.getenv("NVD_API_KEY") != null) {
            String key = System.getenv("NVD_API_KEY");
            if (key != null && key.startsWith("op://")) {
                LOG.warn(
                        "NVD_API_KEY begins with op://; you are not logged in, did not use the `op run` command, or the environment is setup incorrectly");
            } else {
                return key;
            }
            return System.getenv("NVD_API_KEY");
        }
        return apiKey;
    }

    @CommandLine.Option(names = {
            "--apikey"}, description = "NVD API Key; it is highly recommend to set the environment variable NVD_API_KEY instead of using the command line option", interactive = true)
    public void setApiKey(String apiKey) {
        LOG.warn(
                "For easier use - consider setting an environment variable NVD_API_KEY.\n\nSee TODO for more information");
        this.apiKey = apiKey;
    }
}
