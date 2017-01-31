docker run -v `pwd`:/code --cpuset-cpus=3 -it java:openjdk-8 /bin/bash -c cd /code && dd if=/dev/zero of=/dev/null | dd if=/dev/zero of=/dev/null | ./gradlew clean test
