pipeline {
    agent any

    environment {
        GITHUB_CREDENTIALS_ID = 'github-credentials'
        IMAGE_NAME = 'proyecto-backend'
        // Variables para la base de datos temporal de pruebas
        DB_IMAGE = 'mysql:8.0'
        DB_NAME = 'stocktrack-os-7391'
        DB_PASSWORD = '12345678'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    echo 'Levantando base de datos MySQL temporal para los tests...'
                    // Levantamos un contenedor de MySQL en la misma red de Jenkins
                    sh "docker run --name test-db-mysql --network jenkins -d -e MYSQL_ROOT_PASSWORD=${DB_PASSWORD} -e MYSQL_DATABASE=${DB_NAME} -p 3306:3306 ${DB_IMAGE}"

                    // Esperamos unos segundos a que MySQL esté completamente listo para recibir conexiones
                    echo 'Esperando que MySQL inicialice...'
                    sh "sleep 15"

                    try {
                        echo 'Compilando y ejecutando pruebas unitarias con Maven...'
                        // Usamos una imagen de Maven con Java 17/21 (o el wrapper si lo tienes)
                        // Para este entorno local, lo ideal es compilar usando un contenedor para no depender de instalar Java dentro de Jenkins
                        sh "docker run --rm --network jenkins -v \$(pwd):/app -w /app maven:3.9-eclipse-temurin-17 mvn clean test"
                    } finally {
                        echo 'Limpiando el contenedor de la base de datos temporal...'
                        // Esto se ejecuta pase o falle el test, para no dejar basura corriendo
                        sh "docker rm -f test-db-mysql"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Creando la imagen Docker final del Backend...'
                sh "docker build -t ${IMAGE_NAME}:latest ."
            }
        }
    }

    post {
        success {
            echo '¡CI del Backend ejecutado con éxito!'
        }
        failure {
            echo 'Algo falló en el pipeline. Revisa los logs.'
        }
    }
}