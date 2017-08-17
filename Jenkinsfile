#!groovy
node{

    //  env.PATH = "${tool 'Gradle 3.0'}/bin:${env.PATH}"
    currentBuild.result = 'SUCCESS'

    try {

        checkout scm
        def taoName = "tao-core"
        def taoVersion = version()
        currentBuild.description = "${taoName} - ${taoVersion}"
        stage 'Build TAO project'
        try {
            sh 'mvn clean install'
        } catch (err) {
            currentBuild.result = 'UNSTABLE'
            println err
        }

    } catch (err) {
        currentBuild.result = 'FAILURE'
        println err

    } finally {
        notification()
        clearEnv()
    }

}

def version() {
    def matcher = readFile('pom.xml') =~ /version=.*/
    return matcher[0].replaceAll("version=", "")
}

def notification() {
    emailTxt = "<style>\n" +
            "                                  .desc-span{\n" +
            "                                      font-size: 35px;\n" +
            "                                      font-style: oblique;\n" +
            "                                  }\n" +
            "                                  .SUCCESS{\n" +
            "                                      color: darkcyan;\n" +
            "                                  }\n" +
            "                                  .FAILURE{\n" +
            "                                      color: red;\n" +
            "                                  }\n" +
            "                                  .UNSTABLE{\n" +
            "                                      color: darkorange;\n" +
            "                                  }\n" +
            "                                  .desc-orange{\n" +
            "                                      color: brown;\n" +
            "                                  }\n" +
            "                              </style>\n" +
            "\n" +
            "                              <center>\n" +
            "                                  <div><span class=\"desc-orange desc-span\">TAO Integration Test : </span><span class=\"desc-span ${currentBuild.result}\">${currentBuild.result}</span></div>\n" +
            "                                  <p>For more details see : </p>\n" +
            "                                  <p>Job : <a href='${env.BUILD_URL}' target='_top'>Job details</a></p>\n" +
            "                                  <p>IHM Integration Test : <a href='${env.BUILD_URL}/Rapport_de_test_integration/' target='_top'>Integration Test</a></p>\n" +
            "                              </center>";


    emailext(
            subject: "[TAO-JENKINS] ${env.JOB_NAME}[${env.BUILD_NUMBER}] status is [${currentBuild.result}]",
            content: "text/html",
            body: emailTxt,
            to: 'ailioiu@c-s.ro',
            recipientProviders: [[$class: 'DevelopersRecipientProvider']]
    )
}

def clearEnv() {
    try {
        sh '''echo "clean temp directory"
        if [ -z != $BUILD_DIRECTORY ]
        then
            rm -rf ${BUILD_DIRECTORY}
            rm -rf $BUILD_DIRECTORY
        fi
        rm -rf ~/.ssh/known_hosts
        '''
    } catch (err) {
    }
}

