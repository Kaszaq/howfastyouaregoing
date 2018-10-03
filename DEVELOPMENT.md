# DEVELOPMENT

# DEPLOYMENT

## Releasing
1. Trigger pipeline.
2. Go to https://oss.sonatype.org/ to release from staging if everything is ok.

## Testing deployment locally
Follow https://confluence.atlassian.com/bitbucket/debug-your-pipelines-locally-with-docker-838273569.html

Replace `cd` with `pwd` when using unix*.
```
docker run -it --volume=%cd%:/localDebugRepo --workdir="/localDebugRepo" --memory=4g --memory-swap=4g --memory-swappiness=0 --entrypoint=/bin/bash maven:3.3.9
```

### License
Copyright 2017 Michał Kasza

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
