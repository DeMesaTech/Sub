cd /opt/lampp/bin
sudo /opt/lampp/lampp start
cd ./Backend
javac -cp .:lib/mariadb-java-client-3.5.7.jar Server.java && java -cp .:lib/mariadb-java-client-3.5.7.jar -Djdbc.drivers=org.mariadb.jdbc.Driver Server