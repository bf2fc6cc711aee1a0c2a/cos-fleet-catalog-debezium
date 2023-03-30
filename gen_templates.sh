#!/usr/bin/env bash
#
# Generate template from .json

function print_exit() {
    echo $1
    exit 1
}

for CMD in "oc sed"; do
  hash $CMD 2>/dev/null || print_exit "Dependency ${CMD} not met"
done

CONNECTORS_DIR=${1:-descriptors}
TEMPLATE=${2:-templates/cos-fleet-catalog-debezium.yaml}

cat <<EOT > $TEMPLATE
apiVersion: template.openshift.io/v1
kind: Template
name: cos-fleet-catalog-debezium
metadata:
  name: cos-fleet-catalog-debezium
  annotations:
    openshift.io/display-name: Cos Fleet Manager Connector Catalog for Debezium
    description: List of available debezium connectors and metadata
objects:
EOT

echo "Overwriting template ${TEMPLATE}"

for D in "${CONNECTORS_DIR}"/*; do
  CM_NAME=$(basename "${D}")

  echo "Adding configmap ${CM_NAME} to template ${TEMPLATE}"
  echo "-" >> ${TEMPLATE}
  oc create configmap "connector-catalog-debezium-${CM_NAME}" \
    --from-file="${CONNECTORS_DIR}/${CM_NAME}/" \
    --dry-run=client \
    -o yaml | sed -e 's/^/  /' >> $TEMPLATE
done
