pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Asuka-star/TV-redo.git'  // 请替换为您的仓库URL
                echo "Workspace files"
                sh 'ls -la'
            }
        }
        
       stage('Build & Deploy Service') {
            
            // 关键! 为这个阶段指定一个特殊的 agent
            agent {
                docker { 
                    image 'docker/compose:1.29.2' // 启动一个包含 'docker-compose' 命令的工具容器
                    args '-v /var/run/docker.sock:/var/run/docker.sock -u root' // 把“指挥权”交给它
                }
            }
            
            steps {
                script {
                    // 我们现在就在 'docker/compose' 容器里了

                    // 1. 构建所有在 docker-compose.yml 中定义了 'build' 的服务
                    echo "Building Docker images via docker-compose..."
                    sh 'docker-compose build' // (Dockerfile 内部会执行 mvn package)

                    // 2. 部署服务
                    // (请确保你的 docker-compose.yml 设置了 'external' network 指向 'tv-net')
                    echo "Deploying services via docker-compose..."
                    // '-d' = 后台运行
                    // '--no-deps' = 只更新你构建的服务，不要动 Nacos/MySQL
                    // (如果你想一次性部署所有服务，也可以只用 'docker-compose up -d')
                    sh 'docker-compose up -d --no-deps' 

                    // 3. (可选) 清理旧镜像
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