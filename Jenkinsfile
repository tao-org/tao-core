#!groovy
import hudson.plugins.emailext.plugins.recipients.*;

node{

    try {
        currentBuild.result = 'SUCCESS'
        def mycfg_file = 'df97f4a9-f259-4138-bead-21720f9c3b46'

        stage('Checkout sources') {
            checkout scm

            def currentName = "tao-core"
            def currentVersion = version()

            currentBuild.description = "${currentName} - ${currentVersion}"
            env.BUILD_DIRECTORY = "tao-build_" + env.BUILD_ID
        }

        stage ('Prepare environment, clean') {
            echo 'run task --> mvn clean'
            sh '''mvn clean'''
        }
        /*
        try {
            stage 'Build & UT'
            runMavenTasks("check -Dspring.profiles.active=jenkins -i")
        } catch (err) {
            currentBuild.result = 'UNSTABLE'
            println err
        }
        */

        stage ('Install') {
            echo 'run task --> mvn install'
            sh '''mvn install'''
        }

        try {

            //stage 'Sonar Analysis'
            //runMavenTasks("sonarqube -Dspring.profiles.active=jenkins -i")

            stage('Deploy') {
                configFileProvider([configFile(fileId: mycfg_file, variable: 'GLOBAL_MAVEN_SETTINGS')]) {
                    echo "'settings.xml' path: $GLOBAL_MAVEN_SETTINGS"
                    echo "run task --> mvn -s $GLOBAL_MAVEN_SETTINGS deploy"
                    sh '''mvn -s $GLOBAL_MAVEN_SETTINGS deploy'''
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
        stage('Notify'){
            // temporary workaround
            RecipientProviderUtilities.SEND_TO_UNKNOWN_USERS  = true

            emailext(
                subject: "[TAO-JENKINS] Jenkins build '${env.JOB_NAME}[#${env.BUILD_NUMBER}]' status is [${currentBuild.result}]",
                body: "See: ${env.BUILD_URL}",
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
            )
        }

        /*
        stage ('Clean environment') {
            cleanEnv()
        }
        */

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

def cleanEnv() {
    try {
        sh '''echo "clean temp directory"
       if [ -z != /tmp/maven/$BUILD_DIRECTORY ]
       then
           rm -rf /tmp/maven/$BUILD_DIRECTORY
       fi
       '''
    } catch (err) {
    }
}