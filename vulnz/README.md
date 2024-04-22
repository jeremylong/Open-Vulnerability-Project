# vulnz cli

The cli is a spring-boot command line tool built with picocli. The example
below does run the setup - which creates both the `vulnz` symlink (in `/usr/local/bin`)
and a completion script. If using zsh, the completion will be added to 
`/etc/bash_completion.d` or `/usr/local/etc/bash_completion.d` (depending
on if they exist); see [permanently installing completion](https://picocli.info/autocomplete.html#_installing_completion_scripts_permanently_in_bashzsh)
for more details. We may add a brew formula in the future.

After running `install` you may need to restart your shell for the completion to work.

```bash
./gradlew vulnz:build
cd vulnz/build/libs
./vulnz-6.0.1.jar install
vulnz cve --cveId CVE-2021-44228 --prettyPrint
```

Example of using the CLI with an API key stored in [1password](https://1password.com/) using
the `op` CLI (see [getting started with op](https://developer.1password.com/docs/cli/get-started/)):

```bash
export NVD_API_KEY=op://vaultname/nvd-api/credential
eval $(op signin)
op run -- vulnz cve --threads 4 > cve-complete.json
```

## Caching the NVD CVE Data

The vulnz cli can create a cache of the NVD CVE data obtained from the API. The
data is stored in `json` files with the data saved in the traditional yearly groupings
starting with 2002 and going to the current year. In addition, a `cache.properties` is
created that contains the `lastModifiedDate` datetime as well as the prefix used for the
generated JSON files (by default `nvdcve-` is used). Additionally, a `modified` JSON file
is created that will hold the CVEs that have been modified in the last 7 days. After running
the below command you will end up with a directory with:

- `cache.properties`
- `nvdcve-modified.json.gz`
- `nvdcve-modified.meta`
- `nvdcve-2002.json.gz`
- `nvdcve-2002.meta`
- `nvdcve-2003.json.gz`
- `nvdcve-2003.meta`
- ...
- `nvdcve-2024.json.gz`
- `nvdcve-2024.meta`

### API Key is used and a 403 or 404 error occurs

If an API Key is used and you receive a 404 error:

```
ERROR
io.github.jeremylong.openvulnerability.client.nvd.NvdApiException: NVD Returned Status Code: 404
```

There is a good chance that the API Key is set incorrectly or is invalid. To check if the API Key works
the following `curl` command should return JSON:

```
curl -H "Accept: application/json" -H "apiKey: ########-####-####-####-############" -v https://services.nvd.nist.gov/rest/json/cves/2.0\?cpeName\=cpe:2.3:o:microsoft:windows_10:1607:\*:\*:\*:\*:\*:\*:\*
```

If no JSON is returned and you see a 404 error the API Key is invalid and you should request a new one.

### Out-of-Memory Errors

Create the local cache may result in an out-of-memory error. To resolve the
error simply increase the available memory for Java:

```bash
export JAVA_OPTS="-Xmx2g"
```

Alternatively, run the CLI using the `-Xmx2g` argument:

```bash
java -Xmx2g -jar ./vulnz-6.0.1.jar
```

### Creating the Cache

To create a local cache of the NVD CVE Data you can execute the following command
via a daily schedule to keep the cached data current:

```bash
vulnz cve --cache --directory ./cache 
```

Alternatively, without using the above install command:

```bash
./vulnz-6.0.1.jar cve --cache --directory ./cache
```

When creating the cache all other arguments to the vulnz cli
will still work except the `--lastModEndDate` and `--lastModStartDate`.
As such, you can create `--prettyPrint` the cache or create a cache
of only "application" CVE using the `--virtualMatchString=cpe:2.3:a`.

## Docker image

### Configuration

There are a couple of ENV vars

- `NVD_API_KEY`: define your API key
- `DELAY`: override the delay - given in milliseconds. If you do not set an API KEY, the delay will be `10000`

### Run

```bash
# replace the NVD_API_KEY with your NVD api key
docker run --name vulnz -e NVD_API_KEY=myapikey jeremylong/open-vulnerability-data-mirror:6.0.1 

# if you like use a volume 
docker run --name vulnz -e NVD_API_KEY=myapikey -v cache:/usr/local/apache2/htdocs ghcr.io/jeremylong/vulnz:6.0.1

# adjust the memory usage
docker run --name vulnz -e JAVA_OPT=-Xmx2g jeremylong/open-vulnerability-data-mirror:6.0.1

# you can also adjust the delay 
docker run --name vulnz -e NVD_API_KEY=myapikey -e DELAY=3000 jeremylong/open-vulnerability-data-mirror:6.0.1 

```

If you like, run this to pre-populate the database right away

```bash
docker exec -u mirror vulnz /mirror.sh
```

### Build

Assuming the current version is `6.0.1`

```bash
export TARGET_VERSION=6.0.1
./gradlew vulnz:build -Pversion=$TARGET_VERSION
docker build vulnz/ -t ghcr.io/jeremylong/vulnz:$TARGET_VERSION --build-arg BUILD_VERSION=$TARGET_VERSION
```

### Release

```bash
# checkout the repo
git tag vulnz/6.0.1
git push --tags
# this will build vulnz 6.0.1 on publish the docker image tagged 6.0.1 
```
