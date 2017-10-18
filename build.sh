cd src/main 
gulp prod 
cd source_resources/admin-panel
gulp build 
cd ../../../..
gradle build 
cd build/libs
java -jar crmChat.war
