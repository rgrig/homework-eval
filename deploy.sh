TARGET=/var/lib/tomcat6/webapps
rm -rf $TARGET/WebEvalApp/*
cp WebEvalApp.war $TARGET
