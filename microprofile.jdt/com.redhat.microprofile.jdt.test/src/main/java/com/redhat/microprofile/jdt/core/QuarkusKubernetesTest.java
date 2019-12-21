/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package com.redhat.microprofile.jdt.core;

import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertHints;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertHintsDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertProperties;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.assertPropertiesDuplicate;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.h;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.p;
import static com.redhat.microprofile.jdt.internal.core.MicroProfileAssert.vh;

import org.junit.Test;

import com.redhat.microprofile.commons.MicroProfileProjectInfo;

/**
 * Quarkus Kubernetes properties test.
 * 
 * @author Angelo ZERR
 *
 */
public class QuarkusKubernetesTest extends BasePropertiesManagerTest {

	@Test
	public void kubernetes() throws Exception {
		MicroProfileProjectInfo info = getMicroProfileProjectInfoFromMavenProject(MavenProjectName.kubernetes);

		assertProperties(info,

				// io.dekorate.kubernetes.config.KubernetesConfig
				p(null, "kubernetes.name", "java.lang.String", null, true,
						"io.dekorate.kubernetes.config.ApplicationConfiguration", "name", null, 0, null),

				p(null, "kubernetes.readiness-probe.initial-delay-seconds", "int", null, true,
						"io.dekorate.kubernetes.config.Probe", "initialDelaySeconds", null, 0, null),

				p(null, "kubernetes.readiness-probe.initial-delay-seconds", "int", null, true,
						"io.dekorate.kubernetes.config.Probe", "initialDelaySeconds", null, 0, null),

				p(null, "kubernetes.annotations[*].key", "java.lang.String", null, true,
						"io.dekorate.kubernetes.config.Annotation", "key", null, 0, null),

				p(null, "kubernetes.init-containers[*].ports[*].protocol", "io.dekorate.kubernetes.annotation.Protocol",
						null, true, "io.dekorate.kubernetes.config.Port", "protocol", null, 0, null),

				p(null, "kubernetes.deployment.target", "java.lang.String",
						"To enable the generation of OpenShift resources, you need to include OpenShift in the target platforms: `kubernetes.deployment.target=openshift`.\r\n"
								+ "If you need to generate resources for both platforms (vanilla Kubernetes and OpenShift), then you need to include both (coma separated).\r\n"
								+ "`kubernetes.deployment.target=kubernetes, openshift`.",
						true, null, null, null, 0, "kubernetes"),
				p(null, "kubernetes.group", "java.lang.String", null, true,
						"io.dekorate.kubernetes.config.ApplicationConfiguration", "group", null, 0, null),
				p(null, "kubernetes.registry", "java.lang.String", "Specify the `docker registry`.", true, null, null,
						null, 0, null));

		assertPropertiesDuplicate(info);

		assertHints(info,

				h("io.dekorate.kubernetes.annotation.Protocol", null, true,
						"io.dekorate.kubernetes.annotation.Protocol", vh("TCP", null, null), //
						vh("UDP", null, null)));

		assertHintsDuplicate(info);

	}

	@Test
	public void openshift() throws Exception {
		MicroProfileProjectInfo info = getMicroProfileProjectInfoFromMavenProject(MavenProjectName.kubernetes);

		assertProperties(info,

				// io.dekorate.openshift.config.OpenshiftConfig
				p(null, "openshift.name", "java.lang.String", null, true,
						"io.dekorate.kubernetes.config.ApplicationConfiguration", "name", null, 0, null),

				p(null, "openshift.readiness-probe.initial-delay-seconds", "int", null, true,
						"io.dekorate.kubernetes.config.Probe", "initialDelaySeconds", null, 0, null),

				p(null, "openshift.readiness-probe.initial-delay-seconds", "int", null, true,
						"io.dekorate.kubernetes.config.Probe", "initialDelaySeconds", null, 0, null),

				p(null, "openshift.annotations[*].key", "java.lang.String", null, true,
						"io.dekorate.kubernetes.config.Annotation", "key", null, 0, null),

				p(null, "openshift.init-containers[*].ports[*].protocol", "io.dekorate.kubernetes.annotation.Protocol",
						null, true, "io.dekorate.kubernetes.config.Port", "protocol", null, 0, null),

				p(null, "openshift.group", "java.lang.String", null, true,
						"io.dekorate.kubernetes.config.ApplicationConfiguration", "group", null, 0, null),
				p(null, "openshift.registry", "java.lang.String", "Specify the `docker registry`.", true, null, null,
						null, 0, null));

		assertPropertiesDuplicate(info);

		assertHints(info,

				h("io.dekorate.kubernetes.annotation.Protocol", null, true,
						"io.dekorate.kubernetes.annotation.Protocol", vh("TCP", null, null), //
						vh("UDP", null, null)));

		assertHintsDuplicate(info);

	}
}
