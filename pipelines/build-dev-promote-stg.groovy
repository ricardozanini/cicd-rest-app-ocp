def appVersion = ""
def imageTag = ""

pipeline {
    agent any
    stages {
        stage("Build Aplication") {
            agent {
                label 'maven'
            }   
            steps {
                script {
                    def pom = readMavenPom file: "${env.APP_NAME}/pom.xml"
                    appVersion = "${pom.version}-${env.BUILD_NUMBER}"
                    print "Building app ${env.APP_NAME}, version ${appVersion}"
                    sh "cd ${env.APP_NAME} && mvn -B versions:set -DnewVersion=${appVersion}"
                    sh "cd ${env.APP_NAME} && mvn -B -Dmaven.test.skip=true clean package"
                    sh "cp ${env.APP_NAME}/target/${env.APP_NAME}-${appVersion}.jar app.jar"
                    stash name: "artifact", includes: "app.jar"
                }
            }
        }
        stage("Unit Tests") {    
            agent {
                label 'maven'
            }   
            steps {
                script {
                    sh "cd ${env.APP_NAME} && mvn -B clean test"
                    stash name: "unit_tests", includes: "${env.APP_NAME}/target/surefire-reports/**", allowEmpty: true
                }
            }
        }
        stage("Integration Tests") {
            agent {
                label 'maven'
            }
            steps {
                script {
                    sh "cd ${env.APP_NAME} && mvn -B clean verify -Dsurefire.skip=true"
                    stash name: 'it_tests', includes: "${env.APP_NAME}/target/failsafe-reports/**", allowEmpty: true
                }
            }
        }
        stage("Build and Tag Image") {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject("${env.PROJECT_NAME}-dev") {
                            timeout(time:20, unit:'MINUTES') {
                                unstash name: "artifact"
                                imageTag = "v${appVersion}"
                                def buildSelector = openshift.selector("bc/${env.APP_NAME}-docker").startBuild("--from-file='app.jar'")
                                //todo: throw expcetion if doesn't exist
                                buildSelector.logs("-f")
                                def newIsLabels = openshift.selector("is", "${env.APP_NAME}").object()
                                newIsLabels.metadata.labels['org.samples.cicd.build.lastest_commit'] = env.GIT_COMMIT
                                newIsLabels.metadata.labels['org.samples.cicd.build.committer_name'] = env.GIT_COMMITTER_NAME
                                newIsLabels.metadata.labels['org.samples.cicd.build.committer_email'] = env.GIT_COMMITTER_EMAIL
                                newIsLabels.metadata.labels['org.samples.cicd.build.author'] = 'Jenkins'
                                newIsLabels.metadata.labels['org.samples.cicd.build.latest_version'] = appVersion
                                openshift.apply(newIsLabels)
                                openshift.tag("", "${env.APP_NAME}:latest", "${env.APP_NAME}:${imageTag}")
                                def dc  = openshift.selector("dc", env.APP_NAME)
                                dc.rollout().status()
                            }
                        }
                    }
                }
            }
        }
        stage("Approval") {
            agent none
            steps {
                timeout(time:30, unit:'MINUTES') {
                    input 'Do I have your approval to promote this image to stage?' 
                }
            }
        }
        stage("Promote to Staging") {
            agent any
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject("${env.PROJECT_NAME}-stg") {
                            timeout(time:20, unit:'MINUTES') {    
                                //from dev to stg
                                openshift.tag("", "${env.PROJECT_NAME}-dev/${env.APP_NAME}:${imageTag}", "${env.APP_NAME}:${imageTag}")
                                openshift.tag("", "${env.APP_NAME}:${imageTag}", "${env.APP_NAME}:latest")
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



