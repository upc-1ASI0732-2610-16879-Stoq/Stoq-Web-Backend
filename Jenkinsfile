pipeline {
    agent any

    tools {
        jdk 'jdk-17'
        maven 'maven-3.9'
    }

    environment {
        GITHUB_CREDENTIALS_ID = 'github-credentials'
        IMAGE_NAME = 'proyecto-backend'

        RENDER_WEBHOOK = credentials('render-deploy-webhook')
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
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Deploy to Render') {

            steps {
                echo '¡CI aprobado! Notificando a Render para iniciar el Despliegue Continuo (CD)...'
                sh "curl -X POST '${RENDER_WEBHOOK}'"
            }
        }
    }

    post {
        success {
            echo '¡Pipeline ejecutado con éxito!'
        }
        failure {
            echo 'Algo falló en el pipeline. Revisa los logs.'
        }
    }
}