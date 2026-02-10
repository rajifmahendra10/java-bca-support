# BCA Security Demo - Jenkins Integration with JFrog

## 📋 Gambaran Umum

Project ini adalah **demo aplikasi** untuk menunjukkan integrasi **Jenkins CI/CD** dengan **JFrog Artifactory & Xray** untuk security scanning otomatis.

**Tujuan Demo:**
- ✅ Build otomatis Java application di Jenkins
- ✅ Scan dependencies untuk vulnerabilities menggunakan JFrog Xray
- ✅ Block deployment jika ada Critical/High vulnerabilities
- ✅ Upload artifacts ke Artifactory
- ✅ Tracking build dengan Build Info
- ✅ Real-world DevSecOps workflow

---

## 🎯 Vulnerabilities yang Akan Terdeteksi

Project ini **sengaja menggunakan vulnerable dependencies** untuk demo:

| Dependency | Version | CVE | Severity | Description |
|------------|---------|-----|----------|-------------|
| **log4j-core** | 2.14.1 | CVE-2021-44228 | 🔴 **CRITICAL** | Log4Shell RCE vulnerability |
| **log4j-core** | 2.14.1 | CVE-2021-45046 | 🔴 **CRITICAL** | Log4Shell bypass |
| **log4j-core** | 2.14.1 | CVE-2021-45105 | 🟠 **HIGH** | Log4j DoS vulnerability |
| **spring-core** | 5.2.0 | Multiple CVEs | 🟠 **HIGH** | Various Spring vulnerabilities |

**Expected Behavior:**
- Xray akan **detect semua CVEs** di atas
- Policy `bca-security-block-download` akan **trigger violations**
- Build akan **marked as UNSTABLE**
- Deployment akan **BLOCKED** (configurable)

---

## 🏗️ Project Structure

```
sample-java-project/
├── pom.xml                          # Maven configuration dengan vulnerable deps
├── Jenkinsfile                      # CI/CD pipeline definition
├── README.md                        # This file
├── src/
│   └── main/
│       ├── java/
│       │   └── com/bca/demo/
│       │       └── App.java         # Main application
│       └── resources/
│           └── log4j2.xml           # Log4j configuration
└── target/                          # Build output (generated)
    └── bca-security-demo-1.0-SNAPSHOT.jar
```

---

## ⚙️ Setup di Jenkins

### Prerequisites

1. **Jenkins Server:** `http://54.179.2.8:8080/`
2. **JFrog Trial:** `https://trialub2537.jfrog.io`
3. **Jenkins Plugins:**
   - Pipeline
   - Git
   - Credentials Binding
   - Maven Integration (optional tapi recommended)

---

### Step 1: Configure Tools di Jenkins

#### 1.1 Configure JDK
```
Jenkins → Manage Jenkins → Tools → JDK installations
```
- Name: `JDK-11`
- Install automatically: ✅
- Version: Java 11

#### 1.2 Configure Maven
```
Jenkins → Manage Jenkins → Tools → Maven installations
```
- Name: `Maven-3.9.0`
- Install automatically: ✅
- Version: 3.9.0

---

### Step 2: Configure JFrog Credentials

#### 2.1 Add Credentials
```
Jenkins → Manage Jenkins → Credentials → System → Global credentials
```

Klik **"Add Credentials"**:
- **Kind:** Username with password
- **Scope:** Global
- **Username:** `admin` (JFrog username)
- **Password:** `iZeno123!` (JFrog password)
- **ID:** `jfrog-credentials` ← **PENTING: Harus exact ini**
- **Description:** JFrog Artifactory Credentials

Klik **"OK"**

---

### Step 3: Create Pipeline Job

#### 3.1 Create New Item
```
Jenkins Dashboard → New Item
```
- Item name: `BCA-Security-Demo`
- Type: **Pipeline**
- Klik **"OK"**

#### 3.2 Configure Pipeline

**General:**
- Description: `BCA Security Demo - JFrog Xray Integration`
- ✅ Discard old builds (Keep 10 builds)

**Pipeline:**
- Definition: **Pipeline script from SCM**
- SCM: **Git**
- Repository URL: `<Your Git Repo URL jika ada>`
  
  **ATAU jika local:**
- Definition: **Pipeline script**
- Script: Copy-paste isi dari `Jenkinsfile`

**Pipeline Triggers (Optional):**
- ✅ Poll SCM: `H/5 * * * *` (poll every 5 minutes)
- ✅ GitHub hook trigger (jika dari GitHub)

Klik **"Save"**

---

### Step 4: Prepare Project Files

#### 4.1 Upload Project ke Git (Recommended)
```powershell
cd "C:\Users\rajif.mahendra_izeno\Documents\iZeno\BCA\sample-java-project"

git init
git add .
git commit -m "Initial commit: BCA Security Demo with vulnerable deps"

# Push ke GitHub/GitLab
git remote add origin <YOUR_GIT_REPO_URL>
git push -u origin main
```

#### 4.2 OR Setup as Local Project
Jika tidak pakai Git, copy Jenkinsfile content ke Pipeline Script di Jenkins UI.

---

## 🚀 Running the Demo

### Option 1: Trigger Build via Jenkins UI

1. Go to: `http://54.179.2.8:8080/job/BCA-Security-Demo/`
2. Click **"Build Now"**
3. Watch Console Output untuk real-time logs
4. Observe stages:
   - ✅ Checkout
   - ✅ Setup JFrog CLI
   - ✅ Configure JFrog
   - ✅ Build & Test
   - ✅ Package
   - ⚠️ **JFrog Xray Scan** ← Vulnerabilities detected here!
   - ⚠️ Upload to Artifactory
   - ⚠️ Publish Build Info
   - ⚠️ Xray Build Scan
   - 🚨 **Security Gate** ← Build blocked here!

### Option 2: Trigger via CLI

```powershell
# Via Jenkins CLI
java -jar jenkins-cli.jar -s http://54.179.2.8:8080/ build "BCA-Security-Demo"

# OR via curl
curl -X POST http://54.179.2.8:8080/job/BCA-Security-Demo/build
```

---

## 📊 Expected Output

### Console Output Sample:

```
========================================
🛡️ Scanning for vulnerabilities...
========================================
[Info] Scanning JAR file...
[WARN] Vulnerabilities found:
  🔴 CVE-2021-44228 (Log4Shell) - CRITICAL - CVSS 10.0
  🔴 CVE-2021-45046 (Log4Shell bypass) - CRITICAL - CVSS 9.0
  🟠 CVE-2021-45105 (Log4j DoS) - HIGH - CVSS 7.5
  
Total vulnerabilities: 15
Critical: 2
High: 5
Medium: 8

========================================
🚦 Evaluating Security Policy...
========================================
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
========================================
Build completed with status: UNSTABLE
```

---

## 🔍 Verifikasi di JFrog Platform

### 1. Check Violations
```
URL: https://trialub2537.jfrog.io/ui/admin/xray/violations
Filter by: Security_watch_1
```

Expected violations:
- Build: `bca-security-demo #<BUILD_NUMBER>`
- Components affected: log4j-core:2.14.1, spring-core:5.2.0
- Total violations: ~15
- Critical: 2
- High: 5

### 2. Check Uploaded Artifacts
```
URL: https://trialub2537.jfrog.io/ui/repos/tree/General/libs-release-local
Path: /com/bca/demo/bca-security-demo/<BUILD_NUMBER>/
```

File: `bca-security-demo-1.0-SNAPSHOT.jar`

### 3. Check Build Info
```
URL: https://trialub2537.jfrog.io/ui/builds/bca-security-demo/<BUILD_NUMBER>
```

Details akan show:
- Build environment
- Dependencies list
- Published artifacts
- Xray scan results

---

## 🛠️ Troubleshooting

### Issue 1: JFrog CLI Not Found

**Error:**
```
jf: command not found
```

**Solution:**
```powershell
# Manual install di Jenkins node
curl -fL https://install-cli.jfrog.io | sh
chmod +x jf
mv jf /usr/local/bin/
```

### Issue 2: Credentials Not Working

**Error:**
```
401 Unauthorized
```

**Solution:**
1. Verify credentials di Jenkins: Manage Jenkins → Credentials
2. Credential ID harus exact: `jfrog-credentials`
3. Test manual:
   ```powershell
   curl -u admin:iZeno123! https://trialub2537.jfrog.io/artifactory/api/system/ping
   ```

### Issue 3: Maven Not Found

**Error:**
```
mvn: command not found
```

**Solution:**
- Configure Maven di Global Tool Configuration
- OR install manually di Jenkins node
- OR use Maven wrapper (`./mvnw`)

### Issue 4: Build Fails Immediately

**Error:**
```
Failed to connect to JFrog
```

**Solution:**
1. Check JFrog URL: `https://trialub2537.jfrog.io` (no trailing slash)
2. Check network connectivity dari Jenkins node:
   ```powershell
   Test-NetConnection trialub2537.jfrog.io -Port 443
   ```
3. Check firewall rules

---

## 🎓 Demo Script untuk Presentasi BCA

### 1. Introduction (2 menit)
```
"Sekarang kita akan demo bagaimana Jenkins terintegrasi dengan JFrog 
untuk melakukan security scanning otomatis setiap kali ada build baru."
```

### 2. Show Vulnerable Code (2 menit)
```
- Open pom.xml
- Highlight log4j 2.14.1 (VULNERABLE)
- Explain: "Dependency ini punya CVE-2021-44228 (Log4Shell) - Critical"
```

### 3. Trigger Build (1 menit)
```
- Navigate ke Jenkins
- Click "Build Now"
- "Watch real-time scanning process"
```

### 4. Show Scan Results (3 menit)
```
- Point to Console Output
- Highlight detected vulnerabilities
- Show Security Gate block
- Explain: "Build marked as UNSTABLE, deployment blocked"
```

### 5. Show JFrog Platform (3 menit)
```
- Navigate ke Xray Violations
- Show detailed CVE information
- Show Impact Analysis
- Explain: "Xray automatically notifies security team"
```

### 6. Show Fix (2 menit)
```
- Update pom.xml: log4j 2.14.1 → 2.17.1
- Rebuild
- Show: "No violations, build passes!"
```

### 7. Summary (2 menit)
```
"Dengan setup ini, BCA bisa:
✅ Detect vulnerabilities automatically
✅ Block risky deployments
✅ Notify teams instantly
✅ Track compliance over time
✅ Fully automated - no manual intervention needed"
```

---

## 📝 Customization untuk BCA

### Custom Xray Watch Name
Edit Jenkinsfile line 23:
```groovy
XRAY_WATCH = 'bca-production-watch' // Ganti sesuai watch BCA
```

### Custom Fail Behavior
Edit Security Gate stage untuk fail build instead of unstable:
```groovy
if (scanResult == 'UNSTABLE') {
    error("Security violations detected - Build blocked")  // Uncomment this
}
```

### Custom Notification
Tambahkan email notification di `post` block:
```groovy
post {
    unstable {
        emailext (
            subject: "Security Alert: Vulnerabilities in ${BUILD_NAME}",
            body: "Critical vulnerabilities detected. Check Xray for details.",
            to: "security-team@bca.co.id"
        )
    }
}
```

---

## 🔗 Useful Links

- **Jenkins:** http://54.179.2.8:8080/
- **JFrog Platform:** https://trialub2537.jfrog.io
- **Xray Violations:** https://trialub2537.jfrog.io/ui/admin/xray/violations
- **Artifactory:** https://trialub2537.jfrog.io/ui/repos/tree/General/libs-release-local
- **JFrog CLI Docs:** https://jfrog.com/help/r/jfrog-cli

---

## ✅ Success Criteria

Demo berhasil jika:

1. ✅ Build triggered di Jenkins
2. ✅ Xray scan detects CVE-2021-44228 (Log4Shell)
3. ✅ Build marked as UNSTABLE
4. ✅ Violations muncul di Xray UI
5. ✅ Artifact uploaded ke Artifactory (meskipun vulnerable)
6. ✅ Build info published dengan scan results
7. ✅ Security Gate blocks (atau warns) tentang deployment

---

**Document Version:** 1.0  
**Created:** 2026-02-10  
**For:** Bank Central Asia (BCA)  
**By:** iZeno
