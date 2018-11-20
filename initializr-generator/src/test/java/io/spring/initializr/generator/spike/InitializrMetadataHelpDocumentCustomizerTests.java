/*
 * Copyright 2012-2019 the original author or authors.
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

package io.spring.initializr.generator.spike;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.io.template.MustacheTemplateRenderer;
import io.spring.initializr.generator.project.documentation.HelpDocument;
import io.spring.initializr.generator.spike.documentation.InitializrMetadataHelpDocumentCustomizer;
import io.spring.initializr.generator.util.Version;
import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.Link;
import io.spring.initializr.test.metadata.InitializrMetadataTestBuilder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link InitializrMetadataHelpDocumentCustomizerTests}.
 *
 * @author Stephane Nicoll
 */
public class InitializrMetadataHelpDocumentCustomizerTests {

	@Test
	public void contributeLinks() {
		Dependency dependency = Dependency.withId("foo");
		dependency.getLinks()
				.add(Link.create("guide", "example.com/guide", "Test guide"));
		dependency.getLinks().add(
				Link.create("reference", "example.com/doc", "Reference documentation"));
		dependency.getLinks()
				.add(Link.create("other", "example.com/other", "Additional stuff"));
		InitializrMetadata metadata = InitializrMetadataTestBuilder.withDefaults()
				.addDependencyGroup("test", dependency).build();
		ProjectDescription projectDescription = initializeProjectDescription();
		projectDescription.addDependency(dependency.getId(),
				ConceptTranslator.toDependency(dependency));
		HelpDocument document = contributeHelpDocument(projectDescription, metadata);
		assertThat(document.gettingStarted().guides().getItems()).hasSize(1);
		assertThat(document.gettingStarted().guides().getItems().get(0).getHref())
				.isEqualTo("example.com/guide");
		assertThat(document.gettingStarted().guides().getItems().get(0).getDescription())
				.isEqualTo("Test guide");
		assertThat(document.gettingStarted().referenceDocs().getItems()).hasSize(1);
		assertThat(document.gettingStarted().referenceDocs().getItems().get(0).getHref())
				.isEqualTo("example.com/doc");
		assertThat(document.gettingStarted().referenceDocs().getItems().get(0)
				.getDescription()).isEqualTo("Reference documentation");
		assertThat(document.gettingStarted().additionalLinks().getItems()).hasSize(1);
		assertThat(
				document.gettingStarted().additionalLinks().getItems().get(0).getHref())
						.isEqualTo("example.com/other");
		assertThat(document.gettingStarted().additionalLinks().getItems().get(0)
				.getDescription()).isEqualTo("Additional stuff");
	}

	private ProjectDescription initializeProjectDescription() {
		ProjectDescription projectDescription = new ProjectDescription();
		// TODO: could use the metadata to set defaults
		projectDescription.setPlatformVersion(Version.parse("2.0.0.RELEASE"));
		return projectDescription;
	}

	private HelpDocument contributeHelpDocument(ProjectDescription projectDescription,
			InitializrMetadata metadata) {
		HelpDocument document = new HelpDocument(new MustacheTemplateRenderer(""));
		new InitializrMetadataHelpDocumentCustomizer(projectDescription.resolve(),
				metadata).customize(document);
		return document;
	}

}
