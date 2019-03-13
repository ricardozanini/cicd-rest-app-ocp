def tagVersion = ""
def tagsInput = []

pipeline {
    agent any
    stages {
        stage ("Grab available image versions") {
             steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject("${env.PROJECT_NAME}-stg") {
                            def isTags = openshift.selector('istag', [ app: env.APP_NAME ]).objects()
                            for (obj in isTags) {
                                if (obj.tag.name.indexOf("v") >= 0) {
                                    tagsInput.add(obj.tag.name)
                                }
                            }
                        }
                    }
                }
            }
        }
        stage ("Promote to Production") {
            agent none
            steps {
                timeout(time:60, unit:'MINUTES') {
                    script {
                        tagVersion = input (id: 'inputTags', message: 'Which version to promote to production?',  parameters: [ [$class: 'ChoiceParameterDefinition', choices: tagsInput, description: 'Choose the tag to be promoted', name: 'tag'] ])
                        echo "The version choosen is ${tagVersion}"
                        def prdTagVersion = tagVersion.substring(0, tagVersion.indexOf("-"))
                        openshift.withCluster() {
                            openshift.withProject("${env.PROJECT_NAME}-prd") {
                                //from dev to stg
                                openshift.tag("", "${env.PROJECT_NAME}-stg/${env.APP_NAME}:${tagVersion}", "${env.APP_NAME}:${prdTagVersion}")
                                openshift.tag("", "${env.APP_NAME}:${prdTagVersion}", "${env.APP_NAME}:latest")
                                //the dc should trigger
                                def dc  = openshift.selector("dc", env.APP_NAME)
                                dc.rollout().status()
                            }
                        }
                    }
                }
            }
        }
    }
}   