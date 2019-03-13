def tagVersion = ""

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
                                echo isTags
                                for (tag in isTags) {
                                    echo "tag name: ${tag.name}"
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
                    input (id: 'inputTags', message: 'Do I have your approval to promote this image to stage?',  parameters: [ [$class: 'ChoiceParameterDefinition', choices: ['1', '2'], description: 'Choose the tag to be promoted', name: 'tag'] ])
                }
            }
        }
    }
}   