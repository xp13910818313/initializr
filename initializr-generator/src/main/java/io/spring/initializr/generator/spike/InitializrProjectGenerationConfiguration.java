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

import io.spring.initializr.generator.ResolvedProjectDescription;
import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.gradle.ConditionalOnGradle;
import io.spring.initializr.generator.buildsystem.maven.ConditionalOnMaven;
import io.spring.initializr.generator.language.kotlin.ConditionalOnKotlinLanguage;
import io.spring.initializr.generator.packaging.war.ConditionalOnWarPackaging;
import io.spring.initializr.generator.project.ProjectGenerationConfiguration;
import io.spring.initializr.generator.project.code.kotlin.KotlinProjectSettings;
import io.spring.initializr.generator.spike.build.DependencyManagementBuildCustomizer;
import io.spring.initializr.generator.spike.build.InitializrDefaultStarterBuildCustomizer;
import io.spring.initializr.generator.spike.build.InitializrMetadataMavenBuildContributor;
import io.spring.initializr.generator.spike.build.KotlinJpaGradleBuildCustomizer;
import io.spring.initializr.generator.spike.build.KotlinJpaMavenBuildCustomizer;
import io.spring.initializr.generator.spike.build.WarPackagingWebStarterBuildCustomizer;
import io.spring.initializr.generator.spike.code.kotlin.InitializrMetadataKotlinProjectSettings;
import io.spring.initializr.generator.spike.configuration.WebFoldersContributor;
import io.spring.initializr.generator.spike.documentation.InitializrMetadataHelpDocumentCustomizer;
import io.spring.initializr.metadata.InitializrMetadata;

import org.springframework.context.annotation.Bean;

/**
 * Configuration for project contributors that uses the metadata.
 *
 * @author Stephane Nicoll
 */
@ProjectGenerationConfiguration
public class InitializrProjectGenerationConfiguration {

	private final ResolvedProjectDescription projectDescription;

	private final InitializrMetadata metadata;

	public InitializrProjectGenerationConfiguration(
			ResolvedProjectDescription projectDescription, InitializrMetadata metadata) {
		this.projectDescription = projectDescription;
		this.metadata = metadata;
	}

	@Bean
	public DependencyManagementBuildCustomizer dependencyManagementBuildCustomizer() {
		return new DependencyManagementBuildCustomizer(this.projectDescription,
				this.metadata);
	}

	@Bean
	public InitializrDefaultStarterBuildCustomizer defaultStarterBuildCustomizer() {
		return new InitializrDefaultStarterBuildCustomizer(this.metadata);
	}

	@Bean
	@ConditionalOnMaven
	public InitializrMetadataMavenBuildContributor metadataMavenBuildContributor() {
		return new InitializrMetadataMavenBuildContributor(this.projectDescription,
				this.metadata);
	}

	@Bean
	@ConditionalOnWarPackaging
	public WarPackagingWebStarterBuildCustomizer warPackagingWebStarterBuildCustomizer() {
		return new WarPackagingWebStarterBuildCustomizer(this.metadata);
	}

	@Bean
	public WebFoldersContributor webFoldersContributor(Build build) {
		return new WebFoldersContributor(build, this.metadata);
	}

	@Bean
	@ConditionalOnKotlinLanguage
	public KotlinProjectSettings kotlinProjectSettings() {
		return new InitializrMetadataKotlinProjectSettings(this.projectDescription,
				this.metadata);
	}

	@Bean
	@ConditionalOnKotlinLanguage
	@ConditionalOnMaven
	public KotlinJpaMavenBuildCustomizer kotlinJpaMavenBuildCustomizer() {
		return new KotlinJpaMavenBuildCustomizer(this.metadata);
	}

	@Bean
	@ConditionalOnKotlinLanguage
	@ConditionalOnGradle
	public KotlinJpaGradleBuildCustomizer kotlinJpaGradleBuildCustomizer(
			KotlinProjectSettings settings) {
		return new KotlinJpaGradleBuildCustomizer(this.metadata, settings);
	}

	@Bean
	public InitializrMetadataHelpDocumentCustomizer metadataHelpDocumentCustomizer() {
		return new InitializrMetadataHelpDocumentCustomizer(this.projectDescription,
				this.metadata);
	}

}
