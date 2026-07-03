pipeline {
    agent any

    environment {
        GITHUB_CREDENTIALS_ID = 'github-credentials'
        IMAGE_NAME = 'proyecto-backend'
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
                    echo 'Levantando base de datos MySQL temporal usando contenedor auxiliar...'

                    // Usamos una imagen ligera oficial de Docker solo para lanzar el comando run
                    sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock docker:cli docker run --name test-db-mysql --network jenkins -d -e MYSQL_ROOT_PASSWORD=${DB_PASSWORD} -e MYSQL_DATABASE=${DB_NAME} -p 3306:3306 ${DB_IMAGE}"

                    echo 'Esperando que MySQL inicialice...'
                    sh "sleep 15"

                    try {
                        echo 'Compilando y ejecutando pruebas unitarias con Maven...'
                        // Corremos Maven apuntando a la red de Jenkins
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock --network jenkins -v \$(pwd):/app -w /app maven:3.9-eclipse-temurin-17 mvn clean test"
                    } finally {
                        echo 'Limpiando el contenedor de la base de datos temporal...'
                        // Usamos el contenedor auxiliar para borrar el MySQL
                        sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock docker:cli docker rm -f test-db-mysql"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Creando la imagen Docker final del Backend...'
                // Usamos el contenedor auxiliar para construir tu imagen final
                sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v \$(pwd):/app -w /app docker:cli docker build -t ${IMAGE_NAME}:latest ."
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