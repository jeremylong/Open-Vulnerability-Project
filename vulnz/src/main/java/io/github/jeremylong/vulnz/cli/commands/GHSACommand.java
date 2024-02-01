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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.diogonunes.jcolor.Attribute;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.jeremylong.openvulnerability.client.ghsa.GitHubSecurityAdvisoryClient;
import io.github.jeremylong.openvulnerability.client.ghsa.GitHubSecurityAdvisoryClientBuilder;
import io.github.jeremylong.openvulnerability.client.ghsa.SecurityAdvisory;
import io.github.jeremylong.vulnz.cli.model.BasicOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Objects;

import static com.diogonunes.jcolor.Ansi.colorize;

@Component
@CommandLine.Command(name = "ghsa", description = "Client for the GitHub Security Advisory GraphQL API")
public class GHSACommand extends AbstractJsonCommand {
    /**
     * Reference to the logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GHSACommand.class);

    @CommandLine.Option(names = {"--endpoint"}, description = "The GraphQL endpoint of GH or GHE")
    private boolean endpoint;
    @CommandLine.Option(names = {
            "--updatedSince"}, description = "The UTC date/time to filter advisories that were updated since the given date")
    private ZonedDateTime updatedSince;
    @CommandLine.Option(names = {
            "--publishedSince"}, description = "The UTC date/time to filter advisories that were published since the given date")
    private ZonedDateTime publishedSince;
    @CommandLine.Option(names = {
            "--classifications"}, description = "The classification of the advisory (\"GENERAL\", \"MALWARE\")")
    private String classifications;
    // yes - this should not be a string, but seriously down the call path the HttpClient
    // doesn't support passing a header in as a char[]...
    private String apiKey = null;

    /**
     * Returns the GitHub API Token Key if supplied.
     *
     * @return the GitHub API Token Key if supplied; otherwise <code>null</code>
     */
    protected String getApiKey() {
        if (apiKey == null && System.getenv("GITHUB_TOKEN") != null) {
            String token = System.getenv("GITHUB_TOKEN");
            if (token != null && token.startsWith("op://")) {
                LOG.warn(
                        "GITHUB_TOKEN begins with op://; you are not logged in, did not use the `op run` command, or the environment is setup incorrectly");
            } else {
                return token;
            }
        }
        return apiKey;
    }

    @CommandLine.Option(names = {
            "--apikey"}, description = "API Key; it is highly recommend to set the environment variable, GITHUB_TOKEN, instead of using the command line option", interactive = true)
    public void setApiKey(String apiKey) {
        LOG.warn(
                "For easier use - consider setting an environment variable GITHUB_TOKEN.\n\nSee TODO for more information");
        this.apiKey = apiKey;
    }

    @Override
    public Integer timedCall() throws Exception {
        if (isDebug()) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            loggerContext.getLogger("io.github.jeremylong").setLevel(Level.DEBUG);
        }
        GitHubSecurityAdvisoryClientBuilder builder = GitHubSecurityAdvisoryClientBuilder
                .aGitHubSecurityAdvisoryClient().withApiKey(getApiKey());
        if (publishedSince != null) {
            builder.withPublishedSinceFilter(publishedSince);
        }
        if (updatedSince != null) {
            builder.withUpdatedSinceFilter(updatedSince);
        }
        if (classifications != null) {
            builder.withClassifications(classifications);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JsonFactory jfactory = objectMapper.getFactory();
        JsonGenerator jsonOut = jfactory.createGenerator(System.out, JsonEncoding.UTF8);
        if (isPrettyPrint()) {
            jsonOut.useDefaultPrettyPrinter();
        }

        jsonOut.writeStartObject();
        jsonOut.writeFieldName("advisories");
        jsonOut.writeStartArray();
        BasicOutput output = new BasicOutput();
        try (GitHubSecurityAdvisoryClient api = builder.build()) {
            while (api.hasNext()) {
                Collection<SecurityAdvisory> list = api.next();
                if (list != null) {
                    output.setSuccess(true);
                    output.addCount(list.size());
                    for (SecurityAdvisory c : list) {
                        jsonOut.writeObject(c);
                    }
                } else {
                    output.setSuccess(false);
                    output.setReason(String.format("Received HTTP Status Code: %s", api.getLastStatusCode()));
                }
            }
            output.setLastModifiedDate(api.getLastUpdated());
            jsonOut.writeEndArray();
            jsonOut.writeObjectField("results", output);
            jsonOut.writeEndObject();
            jsonOut.close();

            if (!output.isSuccess()) {
                String msg = String.format("%nFAILED: %s", output.getReason());
                LOG.info(colorize(msg, Attribute.RED_TEXT()));
                return 2;
            }
            LOG.info(colorize("\nSUCCESS", Attribute.GREEN_TEXT()));
            return 0;
        } catch (Exception ex) {
            LOG.error("\nERROR", ex);
        }
        return 1;
    }
}
