package io.jenkins.plugins.appcenter;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import io.jenkins.plugins.appcenter.util.MockWebServerUtil;
import io.jenkins.plugins.appcenter.util.TestUtil;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

public class EnvInterpolationTest {

    @ClassRule
    public static JenkinsRule jenkinsRule = new JenkinsRule();

    @Rule
    public MockWebServer mockWebServer = new MockWebServer();

    private FreeStyleProject freeStyleProject;

    @Before
    public void setUp() throws IOException {
        freeStyleProject = jenkinsRule.createFreeStyleProject();
        freeStyleProject.getBuildersList().add(TestUtil.createFileForFreeStyle("three/days/xiola.apk"));

        final EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        final EnvVars envVars = prop.getEnvVars();
        envVars.put("OWNER_NAME", "janes-addiction");
        envVars.put("APP_NAME", "ritual-de-lo-habitual");
        envVars.put("PATH_TO_APP", "three/days/xiola.apk");
        envVars.put("DISTRIBUTION_GROUPS", "casey, niccoli");

        jenkinsRule.jenkins.getGlobalNodeProperties().add(prop);

        final AppCenterRecorder appCenterRecorder = new AppCenterRecorder(
            "at-this-moment-you-should-be-with-us",
            "${OWNER_NAME}",
            "${APP_NAME}",
            "${PATH_TO_APP}",
            "${DISTRIBUTION_GROUPS}"
        );
        appCenterRecorder.setBaseUrl(mockWebServer.url("/").toString());

        freeStyleProject.getPublishersList().add(appCenterRecorder);
    }

    @Test
    public void should_InterpolateEnv_InOwnerName() throws Exception {
        // Given
        MockWebServerUtil.enqueueSuccess(mockWebServer);

        // When
        final FreeStyleBuild freeStyleBuild = freeStyleProject.scheduleBuild2(0).get();

        // Then
        jenkinsRule.assertBuildStatus(Result.SUCCESS, freeStyleBuild);
    }

    @Test
    public void should_InterpolateEnv_InAppPath() throws Exception {
        // Given
        MockWebServerUtil.enqueueSuccess(mockWebServer);

        // When
        final FreeStyleBuild freeStyleBuild = freeStyleProject.scheduleBuild2(0).get();

        // Then
        jenkinsRule.assertBuildStatus(Result.SUCCESS, freeStyleBuild);
    }
}