This repo is based on the [vespa getting started](https://github.com/vespa-engine/sample-apps/tree/master/news)

### Install

You will need `poetry`, `vespa-cli`, and docker / docker-compose on your machine

Run `./install.sh`


### Local debugging

Add this config in your dbug configration (`.vscode/launch.json`) :
```json
{
  "version": "0.2.0",
  "configurations": [
    {
        "type": "java",
        "name": "Debug (Attach) (remote debugging)",
        "request": "attach",
        "hostName": "localhost",
        "port": 5005,
        "timeout": 1000000
    }
  ]
}
```
Check that you have the java debugging options for the jvm. in services.xml you should have something like :
```xml
  <container id="stateless" version="1.0">
    <nodes>
      <jvm options="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"/>
```
You can then launch the debugger, that will will listen to what the jvm running at port 5005 is doing (default vespa debugging port, you can verify with `docker logs <vespa_container_id>`, you should get something like `INFO - container - stdout - Listening for transport dt_socket at address: 5005`). Once it listens to the debugging port, you can launch a query `vespa query 'query=test' searchChain='my-search-chain'`, and vscode will show you the current vespa's jvm execution behavior, if you set breakpoints.