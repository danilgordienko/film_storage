pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/danilgordienko/film_storage.git'
            }
        }

        stage('Build & Deploy Frontend') {
            steps {
                dir('frontend') {
                    sh 'docker-compose down'
                    sh 'docker-compose build'
                    sh 'docker-compose up -d'
                }
            }
        }

	   stage('Build & Deploy Common') {
            steps {
                dir('backend') {
                    sh 'docker-compose down'
                    sh 'docker-compose build'
                    sh 'docker-compose up -d'
                }
            }
        }

        stage('Build & Deploy Service1') {
            steps {
                dir('backend/film_storage') {
                    sh 'docker-compose down'
                    sh 'docker-compose build'
                    sh 'docker-compose up -d'
                }
            }
        }

        stage('Build & Deploy Service2') {
            steps {
                dir('backend/film_fetcher') {
                    sh 'docker-compose down'
                    sh 'docker-compose build'
                    sh 'docker-compose up -d'
                }
            }
        }
    }

    post {
        success {
            echo 'Все сервисы успешно собраны и подняты!'
        }
        failure {
            echo 'Ошибка сборки или запуска.'
        }
    }
}
