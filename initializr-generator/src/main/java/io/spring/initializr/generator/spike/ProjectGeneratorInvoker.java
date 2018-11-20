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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.function.Consumer;

import io.spring.initializr.generator.ProjectDescription;
import io.spring.initializr.generator.ProjectRequest;
import io.spring.initializr.generator.ResolvedProjectDescription;
import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.BuildItemResolver;
import io.spring.initializr.generator.buildsystem.gradle.GradleBuildSystem;
import io.spring.initializr.generator.buildsystem.maven.MavenBuildSystem;
import io.spring.initializr.generator.language.Language;
import io.spring.initializr.generator.packaging.Packaging;
import io.spring.initializr.generator.project.ProjectGenerationContext;
import io.spring.initializr.generator.project.ProjectGenerator;
import io.spring.initializr.generator.project.build.BuildCustomizer;
import io.spring.initializr.generator.project.build.gradle.GradleBuildProjectContributor;
import io.spring.initializr.generator.project.build.maven.MavenBuildProjectContributor;
import io.spring.initializr.generator.spike.build.InitializrMetadataBuildItemResolver;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataProvider;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Invokes the new api based on a {@link ProjectRequest}.
 *
 * @author Stephane Nicoll
 */
public class ProjectGeneratorInvoker {

	private final ApplicationContext parentApplicationContext;

	private final Consumer<AnnotationConfigApplicationContext> projectGenerationContext;

	public ProjectGeneratorInvoker(ApplicationContext parentApplicationContext) {
		this(parentApplicationContext, (context) -> {
		});
	}

	public ProjectGeneratorInvoker(ApplicationContext parentApplicationContext,
			Consumer<AnnotationConfigApplicationContext> projectGenerationContext) {
		this.parentApplicationContext = parentApplicationContext;
		this.projectGenerationContext = projectGenerationContext;
	}

	/**
	 * Generate a project structure for the specified {@link ProjectRequest}. Returns a
	 * directory containing the project.
	 * @param request the project request
	 * @return the generated project structure
	 * @throws IOException if the generation of the project structure failed
	 */
	public Path generateProjectStructure(ProjectRequest request) throws IOException {
		ProjectGenerator projectGenerator = new ProjectGenerator(
				(projectGenerationContext) -> customizeProjectGenerationContext(
						projectGenerationContext, request));
		return projectGenerator.generate(createProjectDescription(request));
	}

	public byte[] generateBuild(ProjectRequest request) throws IOException {
		ProjectGenerator projectGenerator = new ProjectGenerator(
				(projectGenerationContext) -> customizeProjectGenerationContext(
						projectGenerationContext, request));
		ProjectDescription projectDescription = createProjectDescription(request);
		return projectGenerator.generate(projectDescription, this::generateBuild);
	}

	private byte[] generateBuild(ProjectGenerationContext context) throws IOException {
		ResolvedProjectDescription projectDescription = context
				.getBean(ResolvedProjectDescription.class);
		StringWriter out = new StringWriter();
		if (projectDescription.getBuildSystem() instanceof GradleBuildSystem) {
			GradleBuildProjectContributor buildContributor = context
					.getBean(GradleBuildProjectContributor.class);
			buildContributor.writeBuild(out);
		}
		else {
			// Assuming Maven
			MavenBuildProjectContributor buildContributor = context
					.getBean(MavenBuildProjectContributor.class);
			buildContributor.writeBuild(out);
		}
		return out.toString().getBytes();
	}

	private void customizeProjectGenerationContext(
			AnnotationConfigApplicationContext context, ProjectRequest request) {
		context.setParent(this.parentApplicationContext);
		InitializrMetadata metadata = this.parentApplicationContext
				.getBean(InitializrMetadataProvider.class).get();
		context.registerBean(InitializrMetadata.class, () -> metadata);
		context.registerBean(BuildItemResolver.class,
				() -> new InitializrMetadataBuildItemResolver(metadata));
		context.registerBean("temporaryBuildCustomizer", BuildCustomizer.class,
				() -> buildCustomizer(request));
		this.projectGenerationContext.accept(context);
	}

	private ProjectDescription createProjectDescription(ProjectRequest request) {
		ProjectDescription description = new ProjectDescription();
		description.setApplicationName(request.getApplicationName());
		description.setArtifactId(request.getArtifactId());
		description.setBaseDirectory(request.getBaseDir());
		description.setBuildSystem(request.getType().startsWith("gradle")
				? new GradleBuildSystem() : new MavenBuildSystem());
		description.setDescription(request.getDescription());
		description.setGroupId(request.getGroupId());
		description.setJavaVersion(request.getJavaVersion());
		description.setLanguage(Language.forId(request.getLanguage()));
		description.setName(request.getName());
		description.setPackageName(request.getPackageName());
		description.setPackaging(Packaging.forId(request.getPackaging()));
		description.setPlatformVersion(
				ConceptTranslator.toVersion(request.getBootVersion()));
		request.getResolvedDependencies()
				.forEach((dependency) -> description.addDependency(dependency.getId(),
						ConceptTranslator.toDependency(dependency)));
		return description;
	}

	private BuildCustomizer<Build> buildCustomizer(ProjectRequest request) {
		return (build) -> {
			request.getBuildProperties().getVersions()
					.forEach((versionProperty, valueSupplier) -> {
						build.addVersionProperty(
								ConceptTranslator.toVersionProperty(versionProperty),
								valueSupplier.get());
					});
		};
	}

}
