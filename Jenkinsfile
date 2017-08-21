#!groovy
import hudson.plugins.emailext.plugins.recipients.*;

node{

    try {
        currentBuild.result = 'SUCCESS'

        stage('Checkout') {
            checkout scm

            def currentName = "tao-core"
            def currentVersion = version()

            currentBuild.description = "${currentName} - ${currentVersion}"
            env.BUILD_DIRECTORY = "tao-build_" + env.BUILD_ID
        }

        stage ('Prepare environment') {
            sh '''mkdir -p /tmp/maven/$BUILD_DIRECTORY/conf
                mkdir -p /tmp/maven/$BUILD_DIRECTORY/conf
                echo "<settings><localRepository>/tmp/maven/$BUILD_DIRECTORY/repo</localRepository></settings>" >> /tmp/maven/$BUILD_DIRECTORY/conf/settings.xml
                mkdir -p /tmp/maven/$BUILD_DIRECTORY/repo'''
            runMavenTasks("clean")
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

        stage ('Install, skip tests') {
            runMavenTasks("install -DskipTests")
        }

        try {

            //stage 'Sonar Analysis'
            //runMavenTasks("sonarqube -Dspring.profiles.active=jenkins -i")

            stage('Deploy') {
                runMavenTasks("deploy")
            }

            /*
            stage 'Javadoc reporting'
            runMavenTasks("generateJavaDoc")
            publishHTML([
                    allowMissing         : false,
                    alwaysLinkToLastBuild: true,
                    keepAll              : true,
                    reportDir            : "build/docs/javadoc",
                    reportFiles          : "overview-summary.html",
                    reportName           : "${currentName} JavaDoc"
            ])
            */
        } catch (err) {
            currentBuild.result = 'UNSTABLE'
            echo 'An error has occurred. Build status is ' + ${currentBuild.result}
            println err
        }

    } catch (err) {
        currentBuild.result = 'FAILURE'
        echo 'An error has occurred. Build status is ' + ${currentBuild.result}
        println err
    } finally {
        stage('Notify'){
            // temporary workaround
            RecipientProviderUtilities.SEND_TO_UNKNOWN_USERS  = true

            emailext(
                subject: "[TAO-JENKINS] Jenkins job '${env.JOB_NAME}[#${env.BUILD_NUMBER}]' status is [${currentBuild.result}]",
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

def runMavenTasks(tasks) {
    echo 'run task --> mvn ' + tasks
    sh '''mvn ''' + tasks
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