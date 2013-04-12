function configure_westty() {

	local OPTARG

	WESTTY_TAR_URL=
	while getopts "u:" OPTION; do
	case $OPTION in
	u)
	  WESTTY_TAR_URL="$OPTARG"
	  ;;
	esac
	done

	WESTTY_HOME=/usr/local/$(basename $WESTTY_TAR_URL .tar.gz)

	install_tarball $WESTTY_TAR_URL
	ln -s $WESTTY_HOME /usr/local/westty

	if [ -f /etc/profile ]; then
		echo "export WESTTY_HOME=$WESTTY_HOME" >> /etc/profile
		echo 'export PATH=$WESTTY_HOME/bin:$PATH' >> /etc/profile
	fi
	if [ -f /etc/bashrc ]; then
		echo "export WESTTY_HOME=$WESTTY_HOME" >> /etc/bashrc
		echo 'export PATH=$WESTTY_HOME/bin:$PATH' >> /etc/bashrc
	fi
	if [ -f /etc/skel/.bashrc ]; then
		echo "export WESTTY_HOME=$WESTTY_HOME" >> /etc/skel/.bashrc
		echo 'export PATH=$WESTTY_HOME/bin:$PATH' >> /etc/skel/.bashrc
	fi


}

