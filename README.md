# CI/CD Sample for OpenShift with Jenkins

This is a sample project that can be used in demos and/or as reference to build a CI/CD implementation using Jenkins and OpenShift to deploy a Spring Boot REST application. Use at your own discretion.

## How to use

**1)** (Optional) [Deactivate the Jenkins auto-provision](https://docs.openshift.com/container-platform/3.11/install_config/configuring_pipeline_execution.html#overview) on your `master-config.yaml` file.

```yaml
jenkinsPipelineConfig:
  autoProvisionEnabled: false 
```

This is needed to not have Jenkins being auto provisioned on the namespace that will run the pipelines. This way we can keep only one namespace with a single Jenkins installation. Every pipeline on the cluster will use this Jenkins.

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

**4)** After cloning this repo, run the script `install.sh` logged as a `cluster-admin` (hint: `oc login -u system:admin`) to create the projects on your OpenShift cluster for this demo. It'll create three projects: `sample-rest-app-dev`, `sample-rest-app-stg` and `sample-rest-app-prd` with the proper infrastructure.

**5)** On the Jenkins web console, go to Manage Jenkins, Configure System and set this configurations:

a. In the **OpenShift Jenkins Sync** section, Namespace field add `sample-rest-app-dev sample-rest-app-stg`. This will keep the pipelines of your new project in sync with the Jenkins installation.

b. In the **Kubernetes Template** section, add a Persistent Volume Claim to the maven agent pointing to `/home/jenkins/.m2`. This way you'll keep your build java libraries persisted accross pipelines executions.

c. In the same section, in the field **Time in minutes to retain slave when idle**, fill with `1`. The container agent will stay in idle for 1 minute waiting for new pipeline tasks. This is needed because our pipelines set an agent at each step, so reusing agent containers is a rule of thumb.

**6)** Log on your web console, go to the `sample-rest-app-dev`, Builds, Pipelines and hit the **Start Pipeline** button. The application should be deployed on the devlopement environment. After promoting it do the same at the `sample-rest-app-stg` to have the application promoted to production. :)

## Architecture

This is a raw draw of this architecture.

```
            +-------------------------------------------------------------------------------------------------+
            |                                                                                                 |
            |                                               Registry                                          |
            |                                                                                                 |
            |                                                                                                 |
            |                 app:v1.0-10      app:v1.0-10      app:v1.0-10        app:v1.0       app:latest  |
            |                                                                                                 |
            +----------------------+----------------+----------------+----------------+----------------+------+
                                   ^                |                |                ^                |
                                   |                |                |                |                |
                                   |                v                v                |                v
            +------------+   +-----+------+   +-----+------+   +-----+------+   +-----+------+   +-----+------+
            |            |   |            |   |            |   |            |   |            |   |            |
            |            |   |            |   |            |   |            |   |            |   |            |
+---------> |            |   |            |   |            |   |            |   |            |   |            |
            |   Build    |   |    Push    |   |  Dev Test  |   |  QA Test   |   | Final Tag  |   |   Deploy   |
Code Change |            |   |            |   |            |   |            |   |            |   |            |
            |            |   |            |   |            |   |            |   |            |   |            |
            +------------+   +------------+   +------------+   +------------+   +------------+   +------------+
                                                               |            |
                                                               |            |
                              Development                      |  Staging   |              Production
                                                               |            |
                                                               +            +
```

## The Sample REST Application

TBD

## Pipelines

TBD

### Development

TBD

### Staging

TBD

### Production

TBD

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