docker build --rm=false -t oyouin/local-test-image .
docker run -v `pwd`/..:/code -v ~/.m2:/root/.m2 -v ~/.gradle:/root/.gradle -it oyouin/local-test-image /bin/bash -c \
    "cd /code && ./gradlew clean -Dos.name=Linux -Dtest.single=AvitoAdvertsProviderComplexTest \
      --stacktrace --debug test"
