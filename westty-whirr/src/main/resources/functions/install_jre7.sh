#!/bin/bash

function install_jre7() {

  target_dir=/usr/lib/jvm/java-7-oracle

  if [ -e "$target_dir" ]; then
    echo "It appears java is already installed. Skipping java installation."
    echo "Move $target_dir out of the way if you want to reinstall"
    #nasty little hack... somewhere the string 'r e t u r n' gets replaced by exit
    turn=turn
    re$turn
  fi

  tmpdir=`mktemp -d`

  curl http://javadl.sun.com/webapps/download/AutoDL?BundleId=75252 -L --silent --show-error --fail --connect-timeout 60 --max-time 600 --retry 5 -o $tmpdir/java.tar.gz

  (cd $tmpdir; tar xzf java.tar.gz)
  mkdir -p `dirname $target_dir`
  (cd $tmpdir; mv jre* $target_dir)
  rm -rf $tmpdir

  update-alternatives --install /usr/bin/java java $target_dir/bin/java 17000
  update-alternatives --set java $target_dir/bin/java

  # Try to set JAVA_HOME in a number of commonly used locations
  export JAVA_HOME=$target_dir
  if [ -f /etc/profile ]; then
    echo export JAVA_HOME=$JAVA_HOME >> /etc/profile
  fi
  if [ -f /etc/bashrc ]; then
    echo export JAVA_HOME=$JAVA_HOME >> /etc/bashrc
  fi
  if [ -f ~root/.bashrc ]; then
    echo export JAVA_HOME=$JAVA_HOME >> ~root/.bashrc
  fi
  if [ -f /etc/skel/.bashrc ]; then
    echo export JAVA_HOME=$JAVA_HOME >> /etc/skel/.bashrc
  fi
  if [ -f "$DEFAULT_HOME/$NEW_USER" ]; then
    echo export JAVA_HOME=$JAVA_HOME >> $DEFAULT_HOME/$NEW_USER
  fi
}