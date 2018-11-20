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

package io.spring.initializr.generator.spike.build;

import io.spring.initializr.generator.ResolvedProjectDescription;
import io.spring.initializr.generator.buildsystem.BillOfMaterials;
import io.spring.initializr.generator.buildsystem.maven.MavenBuild;
import io.spring.initializr.generator.project.build.BuildCustomizer;
import io.spring.initializr.generator.spike.ConceptTranslator;
import io.spring.initializr.metadata.InitializrConfiguration.Env.Maven;
import io.spring.initializr.metadata.InitializrConfiguration.Env.Maven.ParentPom;
import io.spring.initializr.metadata.InitializrMetadata;

/**
 * A {@link BuildCustomizer} that configures the {@link MavenBuild} based on the metadata.
 *
 * @author Stephane Nicoll
 */
public class InitializrMetadataMavenBuildContributor
		implements BuildCustomizer<MavenBuild> {

	private final ResolvedProjectDescription projectDescription;

	private final InitializrMetadata metadata;

	public InitializrMetadataMavenBuildContributor(
			ResolvedProjectDescription projectDescription, InitializrMetadata metadata) {
		this.projectDescription = projectDescription;
		this.metadata = metadata;
	}

	@Override
	public void customize(MavenBuild build) {
		Maven maven = this.metadata.getConfiguration().getEnv().getMaven();
		String springBootVersion = this.projectDescription.getPlatformVersion()
				.toString();
		ParentPom parentPom = maven.resolveParentPom(springBootVersion);
		if (parentPom.isIncludeSpringBootBom()) {
			String versionProperty = "spring-boot.version";
			BillOfMaterials springBootBom = ConceptTranslator.toBom(this.metadata
					.createSpringBootBom(springBootVersion, versionProperty));
			if (!hasBom(build, springBootBom)) {
				build.addInternalVersionProperty(versionProperty, springBootVersion);
				build.boms().add("spring-boot", springBootBom);
			}
		}
		if (!maven.isSpringBootStarterParent(parentPom)) {
			build.setProperty("project.build.sourceEncoding", "UTF-8");
			build.setProperty("project.reporting.outputEncoding", "UTF-8");
		}
		build.parent(parentPom.getGroupId(), parentPom.getArtifactId(),
				parentPom.getVersion());
	}

	private boolean hasBom(MavenBuild build, BillOfMaterials bom) {
		return build.boms().items()
				.anyMatch((candidate) -> candidate.getGroupId().equals(bom.getGroupId())
						&& candidate.getArtifactId().equals(candidate.getArtifactId()));
	}

}
