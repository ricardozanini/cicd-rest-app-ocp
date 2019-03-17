#!/bin/sh
# TODO: turn this into an Ansilble role. :)

CICD_PROJECT=$1
SAMPLE_PROJECT="sample-rest-app"

# basic validation
validate()
{
    if [ -z "$CICD_PROJECT" ]; then
        echo "Please set a name for the CI/CD project setup on your cluster. Example: ./install.sh cicd"
        exit 1
    fi
}

# create the applications, here we need three envs to promote the images between them
create_projects() 
{
    echo ">>> Creating projects"
    oc new-project "${SAMPLE_PROJECT}-dev"
    oc new-project "${SAMPLE_PROJECT}-stg"
    oc new-project "${SAMPLE_PROJECT}-prd"
}

add_jenkins_permissions()
{
    echo ">>> Adding specific permissions to the system:serviceaccount:${CICD_PROJECT}:jenkins user"
    # for image pulling
    #oc adm policy add-cluster-role-to-user system:image-puller system:serviceaccount:cicd:jenkins
    # TODO: let the jenkins be the admin until we figure out what roles it should have to be able to perform everything it needs.
    oc adm policy add-cluster-role-to-user cluster-admin "system:serviceaccount:${CICD_PROJECT}:jenkins"
}

setup_application() 
{
    env=$1
    pipeline=$2
    project="${SAMPLE_PROJECT}-${env}"

    echo ">>> Creating the infrastructure for the project ${project}."
    oc project $project
    oc process -f openshift/sample-rest-app-infrastructure.json -p NAME=$SAMPLE_PROJECT -p JENKINS_PIPELINE=$pipeline | oc create -f -

    if [ "$env" = "stg" ]; then
        oc delete bc/"${SAMPLE_PROJECT}-docker"
    fi

    if [ "$env" = "prd" ]; then
        oc delete bc --all
    fi
}

validate
create_projects
add_jenkins_permissions

setup_application "dev" "pipelines/build-dev-promote-stg.groovy"
setup_application "stg" "pipelines/promote-prd.groovy"
setup_application "prd"


# (reference only) trigger different deployments
#oc set triggers dc/sample-rest-app --from-image=sample-rest-app:<version> -c sample-rest-app

#oc policy add-role-to-user admin system:serviceaccount:cicd:jenkins -n sample-rest-app-dev
#oc policy add-role-to-user admin system:serviceaccount:cicd:jenkins -n sample-rest-app-stg
#oc policy add-role-to-user admin system:serviceaccount:cicd:jenkins -n sample-rest-app-prd