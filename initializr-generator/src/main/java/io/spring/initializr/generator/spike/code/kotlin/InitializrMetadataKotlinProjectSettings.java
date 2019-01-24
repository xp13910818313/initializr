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

package io.spring.initializr.generator.spike.code.kotlin;

import io.spring.initializr.generator.project.ResolvedProjectDescription;
import io.spring.initializr.generator.project.code.kotlin.KotlinProjectSettings;
import io.spring.initializr.generator.spike.ConceptTranslator;
import io.spring.initializr.metadata.InitializrMetadata;

/**
 * A {@link KotlinProjectSettings} that resolves the version to use from the metadata.
 *
 * @author Stephane Nicoll
 */
public class InitializrMetadataKotlinProjectSettings implements KotlinProjectSettings {

	private final String version;

	public InitializrMetadataKotlinProjectSettings(
			ResolvedProjectDescription projectDescription, InitializrMetadata metadata) {
		this.version = metadata.getConfiguration().getEnv().getKotlin()
				.resolveKotlinVersion(ConceptTranslator
						.fromVersion(projectDescription.getPlatformVersion()));
	}

	@Override
	public String getVersion() {
		return this.version;
	}

}
