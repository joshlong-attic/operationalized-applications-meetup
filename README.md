# operationalized-applications-meetup
sample code from joint meetup with Casey West


# Concourse Deployments

## In Cloud Foundry

You need to specify two user-provided services. First, hosted graphite. You need the `apikey`, `host`, and `port`. Create a JSON file to store them which looks like this:

```json
{
    "apikey": "API-KEY",
    "url": "http://CUSTOM-URL.hostedgraphite.com",
    "port": 12345
}
```

Then create the service in the correct org and space:

```sh
$ cf cups hostedgraphite -p "$(cat hosted-graphite.json)"
```

Next, create a papertrail account and get the `syslog-tls` URL that works for Cloud Foundry integration:

```sh
$ cf cups my-logs -l syslog-tls://HOST.papertrailapp.com:PORT
```

## Setting Your Pipeline

You need to specify hosted graphite variables so the build and basic tests can run for maven install. You need the following environment variables:

* `hostedgraphite-apikey`
* `hostedgraphite-url`
* `hostedgraphite-port`

Make sure they match what you used to configure the user provided service in Cloud Foundry.

You need to specify PWS (or another cloud foundry) credentials and target details with the folloing variables:

* `pws-username`
* `pws-password`
* `pws-organization`
* `pws-space`

Now, set your pipeline. If you want to use the official Concourse v1.1.0 `cf-resource` integration, which may not work due to CF CLI v6.13 from October 2015, do the following:

```sh
$ fly -t lite set-pipeline -p op-apps -c ci/pipeline.yml -l ~/.pws-operationalized.yml
$ fly -t lite unpause-pipeline -p op-apps
$ fly -t lite trigger-job -j op-apps/make-jar-not-war
```

If you want to set a pipeline using an up-to-date CF CLI in the docker container [caseywest/cf-release](https://hub.docker.com/r/caseywest/cf-release/) which packages CF CLI v6.17 from April 2016, do the following:

```sh
$ fly -t lite set-pipeline -p op-apps-working -c ci/working-pipeline.yml -l ~/.pws-operationalized.yml
$ fly -t lite unpause-pipeline -p op-apps-working
$ fly -t lite trigger-job -j op-apps-working/make-jar-not-war
```

Take luck!
