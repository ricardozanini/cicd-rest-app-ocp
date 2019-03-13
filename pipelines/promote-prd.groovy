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
                            timeout(time:5, unit:'MINUTES') {
                                def isTags = openshift.selector('istag', [ app: env.APP_NAME ]).objects()
                                for (obj in isTags) {
                                    tagsInput.put(obj.tag.name)
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
                timeout(time:30, unit:'MINUTES') {
                    tagVersion = input (id: 'inputTags', message: 'Do I have your approval to promote this image to stage?',  parameters: [ [$class: 'ChoiceParameterDefinition', choices: tagsInput, description: 'Choose the tag to be promoted', name: 'tag'] ])
                }
            }
        }
    }
}   