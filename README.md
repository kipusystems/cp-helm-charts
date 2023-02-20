# Deploying

## How to install new or upgrade

```shell
helm --namespace confluent-platform upgrade --install confluent-platform /my-folder/cp-helm-charts -f /my-folder/cp-helm-charts/values.yaml
```

## Install specifying kafka and zookeeper endpoints if you're not running Kafka and Zookeper from this helm

e.g.

```shell
export BOOTSTRAP_SERVERS="PLAINTEXT://broker1:9092"
export ZOOKEEPER_ENDPOINT="z-1:2181"

helm --namespace confluent-platform \
     upgrade --install confluent-platform </path/to/repo>/cp-helm-charts/ \
     -f </path/to/repo>/cp-helm-charts/values.yaml \
     --set cp-control-center.kafka.bootstrapServers=$BOOTSTRAP_SERVERS \
     --set cp-ksql-server.kafka.bootstrapServers=$BOOTSTRAP_SERVERS \
     --set cp-kafka-connect.kafka.bootstrapServers=$BOOTSTRAP_SERVERS \
     --set cp-kafka-connect.cp-schema-registry.url="confluent-platform-cp-schema-registry:8081" \
     --set cp-kafka-rest.cp-zookeeper.url=$ZOOKEEPER_ENDPOINT \
     --set cp-kafka-rest.cp-schema-registry.url="confluent-platform-cp-schema-registry:8081" \
     --set cp-kafka-rest.cp-kafka.bootstrapServers=$BOOTSTRAP_SERVERS \
     --set cp-schema-registry.kafka.bootstrapServers=$BOOTSTRAP_SERVERS
```

## Run with Kafka and Zoopeeker in Kubernetes side

```shell
helm --namespace confluent-platform \
     upgrade --install confluent-platform </path/to/repo>/cp-helm-charts/ \
     -f </path/to/repo>/cp-helm-charts/values.yaml \
     --set cp-kafka-connect.cp-schema-registry.url="confluent-platform-cp-schema-registry:8081" \
     --set cp-kafka-rest.cp-schema-registry.url="confluent-platform-cp-schema-registry:8081" \
     --set cp-zookeeper.enabled=true \
     --set cp-kafka.enabled=true
```

## How to uninstall it all

```shell
helm --namespace confluent-platform uninstall confluent-platform
```

You can check the output of `helm get values <release_name>` to see the values that have been applied to the chart during installation.

e.g.

```shell
helm --namespace confluent-platform get values confluent-platform
```

## Using repo as a submodule of another

When using this repo as a submodule of another (e.g. DevOps repo), you can update the submodule reference as follows:

```shell
cd ../..
git add path/to/submodule
git commit -m "Update submodule to latest commit"
git push origin branch-name
```

For example, in DevOps repo, submodule is configured as:

```shell
[submodule "heml-charts/cp-helm-charts"]
     path = heml-charts/cp-helm-charts
     url = git@github.com:kipusystems/cp-helm-charts.git
```

Therefore, from the root of the DevOps repo, you update to latest commit as needed, and from there follow normal git flow:

```shell
git add heml-charts/cp-helm-charts
git commit -m "[<jira story number>] - Update submodule cp-helm-charts to latest commit"
git push origin branch-name
```

To check the submodule status and determine which branch a Git submodule is pointing to and if it's up-to-date, use the `git submodule` command. This command will display the SHA-1 hash of the commit that the submodule is pointing to, along with the path to the submodule. The SHA-1 hash is a unique identifier for the commit, and it can be used to determine if the submodule is up-to-date:

```shell
git submodule status
```

Check the latest commit in the submodule repository:

```shell
cd path/to/submodule
git log -1
```

This command will display the latest commit in the submodule repository. You can compare the SHA-1 hash of this commit with the SHA-1 hash displayed by the git submodule status command to determine if the submodule is up-to-date.

If the submodule is not up-to-date, you can update it by running the following command from the root of the parent repository:

```shell
git submodule update
```

This command will update the submodule to the latest commit in the remote repository. If you want to update the submodule to a specific branch, you can run the following command:

```shell
cd path/to/submodule
git checkout <branch-name>
cd ../..
git add path/to/submodule
git commit -m "Update submodule to branch <branch-name>"
```

These commands will check out the specified branch in the submodule, stage the changes to the submodule, create a new commit in the parent repository that updates the submodule pointer to the specified branch, and push the changes to the remote repository.

## Using kafka-connect and rest api to work with connectors

You can run these commands from inside the container, or alternatively if there is a load balancer (ingress) then use that instead.

Listing connectors:

```shell
curl http://localhost:8083/connectors --write-out "%{http_code}\\n"
```

Deleting a connector:

```shell
curl -X DELETE http://localhost:8083/connectors/my-connector --write-out "%{http_code}\\n"
```

Creating a new connector:

```shell
curl -X POST -H "Content-Type: application/json" --data '{
  "name": "my-connector",
  "config": {
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "postgres",
    "database.port": "5432",
    "database.user": "debezium",
    "database.password": "dbz",
    "database.dbname": "inventory",
    "table.whitelist": "inventory.customers",
    "slot.name": "my-slot",
    "plugins.dir": "/usr/share/java",
    "plugin.path": "debezium-connector-postgresql"
  }
}' http://localhost:8083>/connectors
```
