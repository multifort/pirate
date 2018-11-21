FROM 139.219.239.226/library/tomcat:8.5.15

ADD target/paas.web.war /usr/local/tomcat/webapps/

RUN mkdir -p /tmp/application_store/picture
RUN sed -i '/<\/Host>/i\       <Context docBase="/tmp/application_store/picture" path="/picture" reloadable="false" />' /usr/local/tomcat/conf/server.xml