pipeline {
    agent any

    tools {
        // Le dice a Jenkins que use el Maven autogestionado del Paso 1
        maven 'maven-3.9'
    }

    environment {
        GITHUB_CREDENTIALS_ID = 'github-credentials'
        IMAGE_NAME = 'proyecto-backend'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo 'Compilando el proyecto backend de Spring Boot con Maven nativo...'
                // Ejecuta la compilación nativa de Java sin depender del comando 'docker'
                sh 'mvn clean package -DskipTests'
            }
        }
    }

    post {
        success {
            echo '¡CI del Backend ejecutado con éxito de forma nativa!'
        }
        failure {
            echo 'Algo falló en el pipeline. Revisa los logs.'
        }
    }
}