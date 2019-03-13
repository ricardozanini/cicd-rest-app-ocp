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
                                def isTags = openshift.selector('istag', [env.APP_NAME]).objects().items()
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
            steps {}
        }
    }
}   