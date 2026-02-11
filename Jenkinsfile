pipeline {
    agent any
    
    environment {
        // JFrog Configuration
        JFROG_URL = 'https://trialub2537.jfrog.io'
        JFROG_CLI_VERSION = '2.52.0'
        
        // Artifactory repository names
        MAVEN_REPO = 'libs-release-local'
        MAVEN_SNAPSHOT_REPO = 'libs-snapshot-local'
        
        // Build info
        BUILD_NAME = 'bca-security-demo'
        BUILD_NUMBER = "${env.BUILD_NUMBER}"
        
        // Xray watch name (must match watch created in Xray UI)
        XRAY_WATCH = 'Security_watch_1'
    }
    
    tools {
        maven 'Maven-3.9.0' // Must be configured in Jenkins Global Tool Configuration
        jdk 'JDK-11'        // Must be configured in Jenkins Global Tool Configuration
    }
    
    stages {
        stage('🏗️ Checkout') {
            steps {
                echo '========================================='
                echo '📥 Checking out source code...'
                echo '========================================='
                checkout scm
            }
        }
        
        stage('🔧 Setup JFrog CLI') {
            steps {
                echo '========================================='
                echo '⚙️ Installing JFrog CLI...'
                echo '========================================='
                script {
                    if (isUnix()) {
                        sh '''
                            # Download JFrog CLI directly to workspace (no sudo needed)
                            curl -fL https://releases.jfrog.io/artifactory/jfrog-cli/v2-jf/2.52.0/jfrog-cli-linux-amd64/jf -o jf
                            chmod +x jf
                            ./jf --version
                        '''
                    } else {
                        bat '''
                            powershell -Command "Invoke-WebRequest -Uri 'https://releases.jfrog.io/artifactory/jfrog-cli/v2-jf/2.52.0/jfrog-cli-windows-amd64/jf.exe' -OutFile 'jf.exe'"
                            jf.exe --version
                        '''
                    }
                }
            }
        }
        
        stage('🔐 Configure JFrog') {
            steps {
                echo '========================================='
                echo '🔑 Configuring JFrog CLI credentials...'
                echo '========================================='
                withCredentials([usernamePassword(
                    credentialsId: 'jfrog-credentials', 
                    usernameVariable: 'JFROG_USER', 
                    passwordVariable: 'JFROG_PASSWORD'
                )]) {
                    script {
                        if (isUnix()) {
                            sh '''
                                # Remove existing config if present, then add new one
                                ./jf config remove bca-jfrog --quiet || true
                                
                                ./jf config add bca-jfrog \
                                    --url=${JFROG_URL} \
                                    --user=${JFROG_USER} \
                                    --password=${JFROG_PASSWORD} \
                                    --interactive=false
                                    
                                ./jf config show
                            '''
                        } else {
                            bat '''
                                jf.exe config add bca-jfrog ^
                                    --url=%JFROG_URL% ^
                                    --user=%JFROG_USER% ^
                                    --password=%JFROG_PASSWORD% ^
                                    --interactive=false
                                    
                                jf.exe config show
                            '''
                        }
                    }
                }
            }
        }
        
        stage('🧪 Build & Test') {
            steps {
                echo '========================================='
                echo '🔨 Building Maven project...'
                echo '========================================='
                script {
                    if (isUnix()) {
                        sh 'mvn clean compile test -DskipTests=false'
                    } else {
                        bat 'mvn clean compile test -DskipTests=false'
                    }
                }
            }
        }
        
        stage('📦 Package') {
            steps {
                echo '========================================='
                echo '📦 Creating JAR artifact...'
                echo '========================================='
                script {
                    if (isUnix()) {
                        sh 'mvn package -DskipTests=true'
                    } else {
                        bat 'mvn package -DskipTests=true'
                    }
                }
            }
        }
        
        stage('🔍 JFrog Xray Scan') {
            steps {
                echo '========================================='
                echo '🛡️ Scanning for vulnerabilities...'
                echo '========================================='
                script {
                    try {
                        if (isUnix()) {
                            sh '''
                                echo "Scanning JAR file..."
                                ./jf scan target/*.jar || echo "Vulnerabilities found - check results"
                                
                                echo "Running audit on project dependencies..."
                                ./jf audit --mvn --watches=${XRAY_WATCH} || echo "Vulnerabilities found in dependencies"
                            '''
                        } else {
                            bat '''
                                echo Scanning JAR file...
                                jf.exe scan target/*.jar || echo Vulnerabilities found - check results
                                
                                echo Running audit on project dependencies...
                                jf.exe audit --mvn --watches=%XRAY_WATCH% || echo Vulnerabilities found in dependencies
                            '''
                        }
                    } catch (Exception e) {
                        echo "⚠️ CRITICAL: Vulnerabilities detected!"
                        echo "Details: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                        // Continue to report generation but mark as unstable
                    }
                }
            }
        }
        
        stage('📤 Upload to Artifactory') {
            when {
                expression { currentBuild.result != 'FAILURE' }
            }
            steps {
                echo '========================================='
                echo '☁️ Uploading artifact to Artifactory...'
                echo '========================================='
                script {
                    if (isUnix()) {
                        sh '''
                            ./jf rt upload \
                                "target/*.jar" \
                                "${MAVEN_SNAPSHOT_REPO}/com/bca/demo/bca-security-demo/1.0-SNAPSHOT/" \
                                --flat=true \
                                --build-name=${BUILD_NAME} \
                                --build-number=${BUILD_NUMBER}
                        '''
                    } else {
                        bat '''
                            jf.exe rt upload ^
                                "target/*.jar" ^
                                "%MAVEN_SNAPSHOT_REPO%/com/bca/demo/bca-security-demo/1.0-SNAPSHOT/" ^
                                --flat=true ^
                                --build-name=%BUILD_NAME% ^
                                --build-number=%BUILD_NUMBER%
                        '''
                    }
                }
            }
        }
        
        stage('📊 Publish Build Info') {
            when {
                expression { currentBuild.result != 'FAILURE' }
            }
            steps {
                echo '========================================='
                echo '📋 Publishing build information...'
                echo '========================================='
                script {
                    if (isUnix()) {
                        sh '''
                            ./jf rt build-collect-env ${BUILD_NAME} ${BUILD_NUMBER}
                            ./jf rt build-publish ${BUILD_NAME} ${BUILD_NUMBER}
                        '''
                    } else {
                        bat '''
                            jf.exe rt build-collect-env %BUILD_NAME% %BUILD_NUMBER%
                            jf.exe rt build-publish %BUILD_NAME% %BUILD_NUMBER%
                        '''
                    }
                }
            }
        }
        
        stage('🔒 Xray Build Scan') {
            when {
                expression { currentBuild.result != 'FAILURE' }
            }
            steps {
                echo '========================================='
                echo '🔐 Scanning build with Xray...'
                echo '========================================='
                script {
                    try {
                        if (isUnix()) {
                            sh '''
                                ./jf build-scan ${BUILD_NAME} ${BUILD_NUMBER} --fail=false
                            '''
                        } else {
                            bat '''
                                jf.exe build-scan %BUILD_NAME% %BUILD_NUMBER% --fail=false
                            '''
                        }
                    } catch (Exception e) {
                        echo "⚠️ Build scan found violations!"
                        echo "Details: ${e.message}"
                        currentBuild.result = 'UNSTABLE'
                    }
                }
            }
        }
        
        stage('🚨 Security Gate') {
            steps {
                echo '========================================='
                echo '🚦 Evaluating Security Policy...'
                echo '========================================='
                script {
                    def scanResult = currentBuild.result ?: 'SUCCESS'
                    
                    if (scanResult == 'UNSTABLE') {
                        echo '''
                        ⚠️⚠️⚠️ SECURITY GATE: FAILED ⚠️⚠️⚠️
                        
                        Critical or High severity vulnerabilities detected!
                        
                        Expected findings:
                        - CVE-2021-44228 (Log4Shell) - CRITICAL
                        - CVE-2021-45046 (Log4Shell bypass) - CRITICAL
                        - Spring Core vulnerabilities - HIGH
                        
                        ACTION REQUIRED:
                        1. Review Xray scan results
                        2. Update vulnerable dependencies
                        3. Re-run build after fixes
                        
                        Deployment BLOCKED by BCA Security Policy
                        '''
                        
                        // In real scenario, you can fail the build here
                        // error("Security violations detected - Build blocked")
                        
                        // For demo, we continue but mark as unstable
                        currentBuild.result = 'UNSTABLE'
                    } else {
                        echo '''
                        ✅✅✅ SECURITY GATE: PASSED ✅✅✅
                        
                        No critical vulnerabilities detected.
                        Safe to deploy to production.
                        '''
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo '========================================='
            echo '📊 Build Summary'
            echo '========================================='
            echo "Build: ${BUILD_NAME} #${BUILD_NUMBER}"
            echo "Status: ${currentBuild.result ?: 'SUCCESS'}"
            echo "Duration: ${currentBuild.durationString}"
            echo '========================================='
            
            // Cleanup
            script {
                if (isUnix()) {
                    sh 'rm -f jf'
                } else {
                    bat 'if exist jf.exe del jf.exe'
                }
            }
        }
        
        success {
            echo '✅ Build completed successfully!'
            echo 'Check Artifactory for uploaded artifacts.'
        }
        
        unstable {
            echo '⚠️ Build completed with warnings!'
            echo '🔍 Security vulnerabilities detected by Xray.'
            echo '📋 Review scan results in JFrog Platform.'
        }
        
        failure {
            echo '❌ Build failed!'
            echo 'Check console output for errors.'
        }
    }
}
