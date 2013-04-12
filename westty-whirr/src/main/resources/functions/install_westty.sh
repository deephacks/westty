function install_westty() {

	cat > /usr/local/westty/conf/westty.properties <<EOF
westty.cluster.ids=$CLUSTER_IDS
westty.public_ip=$PUBLIC_IP
westty.private_ip=$PRIVATE_IP
EOF

}


