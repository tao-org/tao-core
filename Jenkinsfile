#!groovy

node{

    try {
        currentBuild.result = 'SUCCESS'

        stage 'Checkout'
        checkout scm

        def currentName = "tao-core"
        def currentVersion = version()

        currentBuild.description = "${currentName} - ${currentVersion}"
        env.BUILD_DIRECTORY = env.BRANCH_NAME + "_" + env.BUILD_ID

        stage 'Prepare environment'
        runMavenTasks("clean")
        sh '''mkdir -p ~/maven/$BUILD_DIRECTORY/conf'''
        sh '''mkdir -p ~/maven/$BUILD_DIRECTORY/conf
        echo "<settings><localRepository>${HOME}/maven/$BUILD_DIRECTORY/repo</localRepository></settings>" >> ${HOME}/maven/$BUILD_DIRECTORY/conf/settings.xml
        mkdir -p ${HOME}/maven/$BUILD_DIRECTORY/repo'''

        /*
        try {
            stage 'Build & UT'
            runMavenTasks("check -Dspring.profiles.active=jenkins -i")
        } catch (err) {
            currentBuild.result = 'UNSTABLE'
            println err
        }
        */

        stage 'Install'
        runMavenTasks("install")

        try {

            //stage 'Sonar Analysis'
            //runMavenTasks("sonarqube -Dspring.profiles.active=jenkins -i")

            stage 'Deploy'
            runMavenTasks("deploy")

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
            println err
        }

    } catch (err) {
        currentBuild.result = 'FAILURE'
        println err
    } finally {
        stage 'Notify'
        emailext(
                subject: "[TAO-JENKINS] Jenkins job '${env.JOB_NAME}[${env.BUILD_NUMBER}] status is [${currentBuild.result}]",
                body: "See <${env.BUILD_URL}>",
                recipientProviders: [[$class: 'DevelopersRecipientProvider']]
        )
        stage 'Clean environment'
        cleanEnv()
    }
}

def version() {
    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
    matcher ? matcher[0][1] : null
}

def runMavenTasks(tasks) {
    echo 'mvn ' + tasks
    sh '''export M2_HOME=${HOME}/maven/$BUILD_DIRECTORY
          echo "M2_HOME : $M2_HOME"
          mvn ''' + tasks
}

def cleanEnv() {
    try {
        sh '''echo "clean temp directory"
        rm -rf ${HOME}/${BUILD_DIRECTORY}*
        rm -rf ${HOME}/maven/${BUILD_DIRECTORY}*'''
    } catch (err) {
    }
}