def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    stage("checkout") {
        checkout scm
    }

    setJavaTool {
        tool = "ORACLE_JDK8"
    }

    def branch = config.pipelineBranch
    def buildStrategy
    if (branch == "master") {
        buildStrategy = "release"
    }
    else {
        buildStrategy = "snapshot"
    }

    parallel(
        "build, test & publish": {
            stage("build & test") {
                buildProject {
                    strategy = buildStrategy
                }
            }
        }
        ,
        "dependency check": {
            stage("dependency check") {
                dependencyCheck {
                    failOnError = false
                    resultPattern = "**/*dependency-check-report.xml"
                }
            }
        }
    )
    
    stage("sonarqube") {
        mavenSonarPublish {
            sonarServerName = "ADOP Sonar 5_3_or_HIGHER"
            projectName = getRepoName()
            projectBranch = branch
        }
    }    
}
