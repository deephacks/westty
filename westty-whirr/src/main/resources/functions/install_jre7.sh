#!/bin/bash

function install_jre7() {

  jdk_file=jdk-7u21-linux-x64.tar.gz
  jdk_version=jdk1.7.0_21

  target_dir=/usr/lib/jvm

  tmpdir=`mktemp -d`

  curl https://deephacks.googlecode.com/files/jdk-7u21-linux-x64.gz -L --show-error --fail --connect-timeout 60 --max-time 600 --retry 5 -o $tmpdir/java.tar.gz

  (cd $tmpdir; tar xzf java.tar.gz)
  rm -rf $tmpdir/java.tar.gz
  mkdir -p $target_dir
  (cd $tmpdir; mv * $target_dir)
  rm -rf $tmpdir

  update-alternatives --install /usr/bin/java java $target_dir/$jdk_version/bin/java 17000
  update-alternatives --set java $target_dir/$jdk_version/bin/java

  # Try to set JAVA_HOME in a number of commonly used locations
  export JAVA_HOME=$target_dir/$jdk_version
  export PATH=$JAVA_HOME/bin:$PATH
  if [ -f /etc/profile ]; then
    echo export JAVA_HOME=$JAVA_HOME >> /etc/profile
    export PATH=$JAVA_HOME/bin:$PATH >> /etc/profile
  fi
  if [ -f /etc/bashrc ]; then
    echo export JAVA_HOME=$JAVA_HOME >> /etc/bashrc
    echo export PATH=$JAVA_HOME/bin:$PATH >> /etc/bashrc
  fi
  if [ -f ~root/.bashrc ]; then
    echo export JAVA_HOME=$JAVA_HOME >> ~root/.bashrc
    echo export PATH=$JAVA_HOME/bin:$PATH >> ~root/.bashrc
  fi
  if [ -f /etc/skel/.bashrc ]; then
    echo export JAVA_HOME=$JAVA_HOME >> /etc/skel/.bashrc
    echo export PATH=$JAVA_HOME/bin:$PATH >> /etc/skel/.bashrc
  fi
  if [ -f "$DEFAULT_HOME/$NEW_USER" ]; then
    echo export JAVA_HOME=$JAVA_HOME >> $DEFAULT_HOME/$NEW_USER
    echo export PATH=$JAVA_HOME/bin:$PATH >> $DEFAULT_HOME/$NEW_USER
  fi
}