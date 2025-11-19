pipeline {
    agent any
    environment {
        AWS_REGION = 'us-east-1'
        ECR_URI = '711784092484.dkr.ecr.us-east-1.amazonaws.com/project-ecr-repo'
        IMAGE_NAME = 'todoapp'
        IMAGE_TAG = "${BUILD_NUMBER}"
        S3_BUCKET = 'devops-pro-artifacts'
    }
    stages {
        stage('Checkout') {
            steps {
                // Pull code from GitHub
                git branch: 'main', url: 'https://github.com/<your-repo>.git'
            }
        }
        stage('Build JAR') {
            steps {
                sh 'mvn clean package'
            }
        }
        stage('Upload Artifact to S3') {
            steps {
                sh 'aws s3 cp target/maven-0.0.1-SNAPSHOT.jar s3://$S3_BUCKET/$IMAGE_NAME${BUILD_NUMBER}.jar'
            }
        }
        stage('Docker Build & Push to ECR') {
            steps {
                sh '''
                aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_URI
                docker build -t $IMAGE_NAME:$IMAGE_TAG .
                docker tag $IMAGE_NAME:$IMAGE_TAG $ECR_URI:$IMAGE_TAG
                docker tag $IMAGE_NAME:$IMAGE_TAG $ECR_URI:latest
                docker push $ECR_URI:$IMAGE_TAG
                docker push $ECR_URI:latest
                '''
            }
        }
        stage('Load Image into Kind') {
            steps {
                sh 'kind load docker-image $IMAGE_NAME:$IMAGE_TAG --name dev-cluster'
            }
        }
        stage('Deploy to Kind') {
            steps {
                sh '''
                kubectl apply -f deployment.yaml
                kubectl apply -f service.yaml
                kubectl set image deployment/todoapp todoapp=$IMAGE_NAME:$IMAGE_TAG --record
                '''
            }
        }
        stage('Smoke Test') {
            steps {
                sh '''
                ENDPOINT=$(kubectl get svc todoapp-svc -o jsonpath="{.spec.ports[0].nodePort}")
                curl -I http://localhost:$ENDPOINT/
                '''
            }
        }
    }
    post {
        success {
            echo "Deployment successful - $IMAGE_TAG"
        }
        failure {
            echo "Deployment failed"
            sh 'kubectl rollout undo deployment/todoapp'
        }
    }
