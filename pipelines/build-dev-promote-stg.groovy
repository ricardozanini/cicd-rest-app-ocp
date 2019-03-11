def appVersion = ""

pipeline {
    agent any
    stages {
        stage("Build Aplication") {
            agent {
                label 'maven'
            }   
            steps {
                def pom = readMavenPom file: "${env.APP_NAME}/pom.xml"
                appVersion = "${pom.version}-${env.BUILD_NUMBER}"
                sh "cd ${env.APP_NAME}"
                sh "mvn -B versions:set -DnewVersion=${appVersion/}"
                sh "mvn -B -Dmaven.test.skip=true clean package"
                sh "cp target/${env.APP_NAME}-${appVersion}.jar target/app.jar"
                stash name: "artifact", includes: "target/app.jar"
            }
        }
        stage("Unit Tests") {    
            agent {
                label 'maven'
            }   
            steps {
                sh "cd ${env.APP_NAME}"
                sh "mvn -B clean test"
                stash name: "unit_tests", includes: "target/surefire-reports/**"
            }
        }
        stage("Integration Tests") {
            agent {
                label 'maven'
            }
            steps {
                sh "cd ${env.APP_NAME}"
                sh "mvn -B clean verify -Dsurefire.skip=true"
                stash name: 'it_tests', includes: 'target/failsafe-reports/**'
            }
        }
        stage("Build and Tag Image") {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            unstash name: "artifact"
                            def buildSelector = openshift.selector("bc/${env.APP_NAME}-docker").startBuild("--from-file='target/app.jar'")
                            //todo: throw expcetion if doesn't exist
                            buildSelector.logs("-f")        
                            def dc  = openshift.selector("dc", env.APP_NAME)
                            dc.rollout().status()
                        }
                    }
                }
            }
        }
        stage("Approval") {
            agent none
            timeout(time:30, unit:'MINUTES') {
                input 'Do I have your approval to promote this image to stage?'
            }
        }
    }
}



