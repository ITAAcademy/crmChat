cd src/main 
call gulp prod 
cd source_resources/admin-panel
call gulp build 
cd ../../../..
call gradle build 
cd build/libs
call java -jar crmChat.war