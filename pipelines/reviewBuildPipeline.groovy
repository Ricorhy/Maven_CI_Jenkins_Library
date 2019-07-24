properties([gitLabConnection('ADOP Gitlab')])

node("docker") {
    currentBuild.result = "SUCCESS"       
    deleteDir()

    setJavaTool {
        tool = "ORACLE_JDK8"
    }

    gitlabBuilds(builds: ["checkout", "build & test", "dependency check"]) {
        stage("checkout") {
            gitlabCommitStatus("checkout") {
                gitlabCheckout {
                    scheme = "ssh"
                    useServiceName = "gitlab-ce"
                    strategy = "checkout_refs"
                    credentialsId = "adop-jenkins-master"
                }
            }
        }

        parallel(
            "build & test": {
                stage("build & test") {
                    gitlabCommitStatus("build & test") {
                        buildProject {
                            strategy = "review"
                        }
                    }

                }
            },
            "dependency check": {
                stage("dependency check") {
                    gitlabCommitStatus("dependency check") {
                        dependencyCheck {
                            failOnError = false
                            resultPattern = "**/*dependency-check-report.xml"
                        }
                    }
                }
            }
        )

        stage("sonarqube") { 
            gitlabSonarPreview{
                sonarServerName = "ADOP Sonar 5_3_or_HIGHER"
            }
        }
    }
}