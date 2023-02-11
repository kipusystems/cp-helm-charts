How to install new or upgrade

```shell
helm --namespace confluent-platform upgrade --install confluent-platform /my-folder/cp-helm-charts -f /my-folder/cp-helm-charts/values.yaml
```

Install specifying kafka and zookeeper endpoints if you're not running Kafka and Zookeper from this helm:

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

How to uninstall it all

```shell
helm --namespace confluent-platform uninstall confluent-platform
```

You can check the output of `helm get values <release_name>` to see the values that have been applied to the chart during installation.

e.g.

```shell
helm --namespace confluent-platform get values confluent-platform
```
