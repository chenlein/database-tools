FROM 192.168.101.88:5000/dmcop2/java:8
MAINTAINER leichen.china@gmail.com
CMD ["mkdir", "-p", "/root/db_tools"]
WORKDIR /root/db_tools
ADD database-tools-1.0-SNAPSHOT.tar .
RUN ["chmod", "+x", "./database-tools-1.0-SNAPSHOT/bin/database-tools"]
CMD ["./database-tools-1.0-SNAPSHOT/bin/database-tools"]

# docker build -t 192.168.101.88:5000/dmcop2/database-tools:1.0-SNAPSHOT .