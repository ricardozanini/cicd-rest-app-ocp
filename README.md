# CI/CD Sample for OpenShift with Jenkins

This is a sample project that can be used in demos and/or as reference to build a CI/CD implementation using Jenkins and OpenShift to deploy a Spring Boot REST application. Use at your own discretion.

## How to use

**1)** (Optional) [Deactivate the Jenkins auto-provision](https://docs.openshift.com/container-platform/3.11/install_config/configuring_pipeline_execution.html#overview) on your `master-config.yaml` file.

```yaml
jenkinsPipelineConfig:
  autoProvisionEnabled: false 
```

This is needed to not have Jenkins being auto provisioned in the namespace that will run the pipelines. This way we can keep only one namespace with a single Jenkins installation. Every pipeline on the cluster will use this Jenkins.

If you're using Minishift, this file is under those three directories:

```
/var/lib/minishift/base/kube-apiserver/master-config.yaml
/var/lib/minishift/base/openshift-apiserver/master-config.yaml
/var/lib/minishift/base/openshift-controller-manager/master-config.yaml
```

Change all of them and restart your Minishift (or the atomic service if on a cluster).  

**2)** Install Jenkins in your cluster. [Follow the directions on the OpenShift documentation](https://docs.openshift.com/container-platform/3.11/dev_guide/dev_tutorials/openshift_pipeline.html#creating-the-jenkins-master) by running the `jenkins-persistent` (or `jenkins-ephemeral`) template:

```shell
$ oc project <project_name> 
$ oc new-app jenkins-persistent
```

*Keep in mind that the `jenkins-ephemeral` won't persist your configuration between pod restarts.*

**4)** After cloning this repo, run the script `install.sh` logged as a `cluster-admin` (hint: `oc login -u system:admin`) to create the projects and the infrastrucuture for this demo in your OpenShift cluster. It'll create three projects: `sample-rest-app-dev`, `sample-rest-app-stg` and `sample-rest-app-prd` with the proper infrastructure.

**5)** In the Jenkins web console, go to *Manage Jenkins*, *Configure System* and set the following configurations:

a. In the **OpenShift Jenkins Sync** section, *Namespace* field add `sample-rest-app-dev sample-rest-app-stg`. This will keep the pipelines of your new project in sync with the Jenkins installation.

b. In the **Kubernetes Template** section, add a *Persistent Volume Claim* to the maven agent pointing to `/home/jenkins/.m2`. This way you'll keep your build Java dependencies persisted across pipelines executions.

c. In the same section, in the field **Time in minutes to retain slave when idle**, fill with `1`. The container agent will stay in idle for 1 minute waiting for new pipeline tasks. This is needed because our pipelines set an agent at each step, so reusing agent containers is a rule of thumb.

**6)** Log on your web console, go to the `sample-rest-app-dev`, *Builds*, *Pipelines* and hit the **Start Pipeline** button. The application should be deployed on the development environment. After promoting it do the same at the `sample-rest-app-stg` to have the application promoted to production. :)

## CI/CD Flow

The schema bellow illustrates the pipelines flow across all the three environments (development, staging and production).

```
                                                                                                   +
                                                                                                   |
Development                                                                                        |  Staging
                                                                                                   |
+--------------------------------------------------------------------------------------------------+----------->

+-------------+ +-------+ +-----------+ +-------------+ +-------------+ +-----------+ +----------+ +-----------+
|             | |       | |           | |             | |             | |           | |          | |           |
|     New     | |       | |           | | Integration | |    Build    | |           | |          | |  Staging  |
|             | | Build | | Unit Test | |             | |             | | Tag Image | | Approval | |           |
| Version Set | |       | |           | |    Test     | |    Image    | |           | |          | | Promotion |
|             | |       | |           | |             | |             | |           | |          | |           |
+-------------+ +-------+ +-----------+ +-------------+ +-------------+ +-----------+ +----------+ +-----------+

                 +
                 |
Staging          |  Production
                 |
+----------------+--------------------------->

+-------------+  +-----------+  +------------+
|             |  |           |  |            |
| Elect Image |  | Tag Final |  | Production |
|             |  |           |  |            |
|   Version   |  |  Version  |  | Promotion  |
|             |  |           |  |            |
+-------------+  +-----------+  +------------+
```

In a nutshell, there are two pipelines:

- [`build-dev-promote-stg.groovy`](/pipelines/build-dev-promote-stg.groovy): deployed on the development project. It's responsible for generate a new application build and promote it to staging after approval
- [`promote-prd.groovy`](/pipelines/promote-prd.groovy): deployed on the staging project. It reads the images tags from the repository and handle to the user to elect one release candidate to promote to production. The promote process is a simple image tag on the target environment

## The Sample REST Application

The sample application is implemented with Spring Boot framework that publishes the `/info` REST endpoint. By calling this endpoint you can inspect some internals of the app like the published version and the pod where it's running:

```
{
  "version": "1.0-1",
  "podName": "sample-rest-app-1-sv7gj",
  "username": "jboss"
}
```

## Known Issues

- The Jenkins user must have `cluster-admin` privileges to be able to tag the images across projects. The ideal is to create a cluster role that specifies the verbs and resources that the Jenkins user should have to run the pipelines. Contributions are welcome. :)

## References

- [Application CI/CD on OpenShift Container Platform with Jenkins](https://access.redhat.com/documentation/en-us/reference_architectures/2017/html-single/application_cicd_on_openshift_container_platform_with_jenkins/index)
- [OpenShift Docs - Build Inputs](https://docs.openshift.com/container-platform/3.11/dev_guide/builds/build_inputs.html#binary-source)
- [OpenShift Docs - Docker Strategy Options](https://docs.openshift.com/container-platform/3.11/dev_guide/builds/build_strategies.html#docker-strategy-options)
- [Enhancing your Builds on OpenShift: Chaining Builds](https://blog.openshift.com/chaining-builds/)
- [OpenShift Docs - Binary Builds](https://docs.openshift.com/container-platform/3.11/dev_guide/dev_tutorials/binary_builds.html)
- [OpenShift Docs - Pipeline Builds](https://docs.openshift.com/container-platform/3.11/dev_guide/dev_tutorials/openshift_pipeline.html)
- [OpenShift Docs - Cross Project Access](https://docs.openshift.com/container-platform/3.11/using_images/other_images/jenkins.html#jenkins-cross-project-access)
- [OpenShift Docs - Configuring Pipeline Execution](https://docs.openshift.com/container-platform/3.11/install_config/configuring_pipeline_execution.html#overview)
- [Promoting Applications Across Environments](https://docs.openshift.com/container-platform/3.11/dev_guide/application_lifecycle/promoting_applications.html)