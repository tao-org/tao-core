#!groovy
import hudson.plugins.emailext.plugins.recipients.*;

node{

    try {
        currentBuild.result = 'SUCCESS'
        def mycfg_file = 'df97f4a9-f259-4138-bead-21720f9c3b46'

        stage('Checkout sources & clean') {
            checkout scm

            //def currentName = "tao-core"
            //def currentVersion = version()
            //currentBuild.description = "${currentName} - ${currentVersion}"

            echo 'run task --> mvn clean -U'
            sh '''mvn clean -U'''
        }

        try {
            stage('Build & UT') {
                echo 'run task --> mvn install'
                sh '''mvn install'''
            }
        } catch (err) {
            currentBuild.result = 'UNSTABLE'
            println err
        }

        try {
            stage ('Sonar analysis'){
                echo 'run task --> mvn sonar:sonar'
                sh '''mvn sonar:sonar'''
            }

            stage('Deploy') {
                configFileProvider([configFile(fileId: mycfg_file, variable: 'GLOBAL_MAVEN_SETTINGS')]) {
                    echo "'settings.xml' path: $GLOBAL_MAVEN_SETTINGS"
                    echo "run task --> mvn -s $GLOBAL_MAVEN_SETTINGS default:deploy"
                    sh '''mvn -s $GLOBAL_MAVEN_SETTINGS default:deploy'''
                }
            }
        } catch (err) {
            currentBuild.result = 'UNSTABLE'
            echo 'An error has occurred. Build status is ' + "${currentBuild.result}"
            println err
        }

    } catch (err) {
        currentBuild.result = 'FAILURE'
        echo 'An error has occurred. Build status is ' + "${currentBuild.result}"
        println err
    } finally {
        stage('Email notification'){
            // temporary workaround
            RecipientProviderUtilities.SEND_TO_UNKNOWN_USERS  = true

            emailext(
                subject: "[TAO-JENKINS] Jenkins build '${env.JOB_NAME}[#${env.BUILD_NUMBER}]' status is [${currentBuild.result}]",
                body: "See: ${env.BUILD_URL}",
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }
    }
}

/*
def version() {
    pom = readMavenPom file: 'pom.xml'
    return pom.version
}
*/

def version() {
    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
    return matcher ? matcher[0][1] : null
}