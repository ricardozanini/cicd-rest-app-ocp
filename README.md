## CI/CD Sample for OpenShift with Jenkins

This is a sample project that can be used in demos and/or as reference to build a CI/CD implementation using Jenkins and OpenShift to deploy a Spring Boot REST application. Use at your own discretion.

### How to use

1. (Optional) [Deactivate the Jenkins auto-provision](https://docs.openshift.com/container-platform/3.11/install_config/configuring_pipeline_execution.html#overview) on your `master-config.yaml` file.

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

2. Install Jenkins in your cluster. Follow the directions on the OpenShift documentation by running the `jenkins-ephemeral` template.

3. The `jenkins-ephemeral` won't persist your data, so attach a Volume to it pointing to `/var/home/jenkins`. 

4. After cloning this repo, run the script `install.sh` logged as a `cluster-admin` (hint: `oc login -u system:admin`) to create the projects on your OpenShift cluster for this demo. It'll create three projects: `sample-rest-app-dev`, `sample-rest-app-stg` and `sample-rest-app-prd` with the proper infrastructure.

5. Log on your web console, go to the `sample-rest-app-dev`, Builds, Pipelines and hit the **Start Pipeline** button. The application should be deployed on the devlopement environment. After promoting it do the same at the `sample-rest-app-stg` to have the application promoted to production. :)

### Architecture

### The Sample REST Application

TBD

### Pipelines

TBD

#### Development

TBD

#### Staging

TBD

#### Production

TBD

### References

- [Application CI/CD on OpenShift Container Platform with Jenkins](https://access.redhat.com/documentation/en-us/reference_architectures/2017/html-single/application_cicd_on_openshift_container_platform_with_jenkins/index)
- [OpenShift Docs - Build Inputs](https://docs.openshift.com/container-platform/3.11/dev_guide/builds/build_inputs.html#binary-source)
- [OpenShift Docs - Docker Strategy Options](https://docs.openshift.com/container-platform/3.11/dev_guide/builds/build_strategies.html#docker-strategy-options)
- [Enhancing your Builds on OpenShift: Chaining Builds](https://blog.openshift.com/chaining-builds/)
- [OpenShift Docs - Binary Builds](https://docs.openshift.com/container-platform/3.11/dev_guide/dev_tutorials/binary_builds.html)
- [OpenShift Docs - Pipeline Builds](https://docs.openshift.com/container-platform/3.11/dev_guide/dev_tutorials/openshift_pipeline.html)
- [OpenShift Docs - Cross Project Access](https://docs.openshift.com/container-platform/3.11/using_images/other_images/jenkins.html#jenkins-cross-project-access)
- [OpenShift Docs - Configuring Pipeline Execution](https://docs.openshift.com/container-platform/3.11/install_config/configuring_pipeline_execution.html#overview)
- [Promoting Applications Across Environments](https://docs.openshift.com/container-platform/3.11/dev_guide/application_lifecycle/promoting_applications.html)