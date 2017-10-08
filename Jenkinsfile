pipeline {

    agent { 
	docker {
	    image 'maven:3.3.3' 
	    args '-v /root/.m2:/root/.m2'
    }

    stages {
        stage('Build jar') {
            steps {
                sh 'mvn -B -DskipTests=true clean package'
            }
        }

        stage('Unit tests') {
            steps {
		# run unit tests
                sh 'mvn -B -Dmaven.test.failure.ignore=true test'
		# generate unit tests reports
		sh 'mvn -B -DgenerateReport=false surefire-report:report site'
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
        success {
            echo 'Build successful!:)'
        }
    }
                                                                                                                                   1,1           Top
}
