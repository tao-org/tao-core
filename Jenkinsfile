#!groovy

node{

    try {
        currentBuild.result = 'SUCCESS'

        echo 'version = ' + version()
        echo 'version1 = ' + version1()

        stage 'Checkout'
        checkout scm

        def currentName = "tao-core"
        def currentVersion = version()

        currentBuild.description = "${currentName} - ${currentVersion}"
        env.BUILD_DIRECTORY = env.BRANCH_NAME + "_" + env.BUILD_ID

        stage 'Prepare environment'
        sh '''mkdir -p /tmp/maven/$BUILD_DIRECTORY/conf'''
        sh '''mkdir -p /tmp/maven/$BUILD_DIRECTORY/conf
        echo "<settings><localRepository>/tmp/maven/$BUILD_DIRECTORY/repo</localRepository></settings>" >> /tmp/maven/$BUILD_DIRECTORY/conf/settings.xml
        mkdir -p /tmp/maven/$BUILD_DIRECTORY/repo'''
        runMavenTasks("clean")
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
    pom = readMavenPom file: 'pom.xml'
    return pom.version
}

def version1() {
    def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'
    matcher ? matcher[0][1] : null
}

def runMavenTasks(tasks) {
    echo 'mvn ' + tasks
    sh '''export M2_HOME=/tmp/maven/$BUILD_DIRECTORY
          echo "M2_HOME : $M2_HOME"
          mvn ''' + tasks
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