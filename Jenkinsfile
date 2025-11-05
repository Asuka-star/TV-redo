pipeline {
    agent any

    tools {
        maven 'Default Maven'  // 请确保 Jenkins 中配置了名为 'Default Maven' 的 Maven 安装
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Asuka-star/TV-redo.git'  // 请替换为您的仓库URL
                echo "Workspace files"
                sh 'ls -la'
            }
        }

       stage('Maven Build') {
            steps {
                echo "Building all Maven modules..."
                // Jenkins 直接在宿主机上运行 mvn (会使用 /var/lib/jenkins/.m2 缓存)
                sh 'mvn clean package -DskipTests'
            }
        }


       stage('Build & Deploy Service') {
            steps {
                script {
                    echo "Building Docker images (fast)..."
                    // Jenkins 直接在宿主机上运行 docker-compose
                    sh 'docker-compose build'

                    echo "Deploying services..."
                    // (请确保你的 docker-compose.yml 修复了端口冲突)
                    sh 'docker-compose up -d --no-deps'

                    echo "Cleaning up old images..."
                    sh 'docker image prune -f'
                }
            }
        }
    }
    
    
    post {
        always {
            echo 'Pipeline执行完成。'
        }
        success {
            echo '部署成功！'
        }
        failure {
            echo '部署失败，请检查日志。'
        }
    }
}