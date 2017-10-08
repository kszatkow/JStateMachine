pipeline {

    agent { 
	docker {
	    image 'maven:3.3.3' 
	    args '-v /root/.m2:/root/.m2'
	}
    }

    stages {
        stage('Build jar') {
            steps {
                sh 'mvn -B -DskipTests=true clean package'
            }
        }

        stage('Unit tests') {
            steps {
		// run unit tests
                sh 'mvn -B -Dmaven.test.failure.ignore=true test'
		// generate unit tests reports
		sh 'mvn -B -DgenerateReport=false surefire-report:report site'
            }
        }

	stage('SonarQube analysis') {
    	    steps {
		withSonarQubeEnv('Local sonar') {
                    // requires SonarQube Scanner for Maven 3.2+
                    sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar'
            	}
	    }
        }

        stage('Site') {
            steps {
                sh 'mvn -B site'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying'
            }
        }
    }

    post {
        always {
	    // archive produced jar
            archive 'target/*.jar'
	    // archive site with all the reports
 	    archive 'target/site/**/*'
	    // generate special junit report
	    junit 'target/surefire-reports/**/*.xml'
        }
    }
                                                                                         
}
