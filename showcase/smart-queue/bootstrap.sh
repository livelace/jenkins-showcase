#!/usr/bin/env bash

CASC_JENKINS_CONFIG="/var/jenkins_home/showcase/smart-queue/iac/jenkins.yaml"
JAVA_OPTS="-Xmx2048m -Djava.awt.headless=true -Djenkins.model.Jenkins.crumbIssuerProxyCompatibility=true -Dpermissive-script-security.enabled=no_security -Dorg.jenkinsci.main.modules.sshd.SSHD.idle-timeout=0 -Djenkins.install.runSetupWizard=false"

docker run -ti --rm \
  --env CASC_JENKINS_CONFIG="$CASC_JENKINS_CONFIG" \
  --env JAVA_OPTS="$JAVA_OPTS" \
  --volume "${PWD}/config":"/var/jenkins_home/showcase/smart-queue/config" \
  --volume "${PWD}/iac":"/var/jenkins_home/showcase/smart-queue/iac" \
  --volume "${PWD}/scriptler":"/var/jenkins_home/scriptler" \
  livelace/jenkins:latest