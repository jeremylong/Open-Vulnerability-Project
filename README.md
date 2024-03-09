# Open Vulnerability Project

The Open Vulnerability Project is a collection of Java libraries and a CLI to work
with various vulnerability data-sources (NVD, GitHub Security Advisories, CISA Known Exploited Vulnerablity Catalog, FIRST Exploit Prediction Scoring System (EPSS), etc.).

- [open-vulnerability-clients](/open-vulnerability-clients) is a collection of clients to retrieve vulnerability data from various data-feeds and APIs.
- [vulnz](/vulnz) a simple CLI that can be used to access the vulnerability sources and persist the data using the open-vulnerability-store.

## Caching the NVD CVE API Data

One of the primary uses of the vulnz CLI is to be able to create a local cache of
the NVD CVE Data from their API. See the [vulnz/README.md](/vulnz/README.md#caching-the-nvd-cve-data)
for instructions on how to create and maintain the local cache.

## Upgrading from vuln-tools

The project started off called vuln-tools and the various APIs were seperated into
standalone JAR files. The project has been renamed to the Open Vulnerability Project.

- All of the client libraries are now in the [open-vulnerability-clients](/open-vulnerability-clients).
- Packages have been renamed/moved:
    - `io.github.jeremylong.ghsa.*` -> `io.github.jeremylong.openvulnerability.client.ghsa.*`
    - `io.github.jeremylong.nvdlib.*` -> 'io.github.jeremylong.openvulnerability.client.nvd.*'
    - `io.github.jeremylong.nvdlib.nvd` -> 'io.github.jeremylong.openvulnerability.client.nvd.*'
- The `NvdCveApi` class has been renamed to `NvdCveClient`.
