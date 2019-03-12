def appVersion = ""

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
                        openshift.withProject() {
                            timeout(time:20, unit:'MINUTES') {
                                unstash name: "artifact"
                                def buildSelector = openshift.selector("bc/${env.APP_NAME}-docker").startBuild("--from-file='app.jar'")
                                //todo: throw expcetion if doesn't exist
                                buildSelector.logs("-f")        
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
    }
}



