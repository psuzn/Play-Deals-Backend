set dotenv-load

default:
    @just --list

namespace:= "play-deals-backend"
serviceAccountName:= "play-deals-backend-deployer"

dev-run-backend:
  ./gradlew backend:run

ns-create:
    kubectl create namespace {{ namespace }}

ns-delete:
    kubectl delete namespace {{ namespace }}

helm-create-deployer:
    echo "Creating deployer service account to $(kubectl config current-context)"
    helm install play-deals-backend-deployer ./helm/deployer \
        --namespace {{namespace}}  --create-namespace \
        --set serviceAccountName={{serviceAccountName}} \
        --set namespace={{namespace}}

deployer-cubeconfig:
    #!/usr/bin/env sh
    CLUSTER_NAME=$(kubectl config current-context)
    SECRET_NAME="sa-{{ serviceAccountName }}-token"
    SA_TOKEN=$(kubectl get secret $SECRET_NAME -n {{namespace}}  -o jsonpath='{.data.token}' | base64 -D)
    CA_DATA=$(kubectl get secret $SECRET_NAME -n {{namespace}} -o jsonpath='{.data.ca\.crt}')
    K8S_ENDPOINT=$(kubectl config view -o jsonpath="{.clusters[?(@.name=='${CLUSTER_NAME}')].cluster.server}")
    echo "
    apiVersion: v1
    kind: Config
    clusters:
      - name: default-cluster
        cluster:
          certificate-authority-data: ${CA_DATA}
          server: ${K8S_ENDPOINT}
    contexts:
    - name: default-context
      context:
        cluster: default-cluster
        namespace: {{namespace}}
        user: default-user
    current-context: default-context
    users:
    - name: default-user
      user:
        token: ${SA_TOKEN}
    "

helm-delete-deployer:
    echo "Deleting deployer service account to $(kubectl config current-context)"
    helm uninstall play-deals-backend-deployer  --namespace {{namespace}}


